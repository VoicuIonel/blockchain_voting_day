import pandas as pd
import os
import psycopg2
from pathlib import Path
from insert_class import inserare_date
import getpass


dpath = os.getcwd()
gdpath = f"{str(Path(dpath).parents[0])}\\2. Generare Date"
ltables = ['ziua_votarii', 'candidati', 'populatie', 'judete']
lviews = ["rezultate_per_judete"]

h, d, u, p = "localhost", "staging", input("User:"), getpass.getpass('Password:')

commands_tables = (
        """CREATE TABLE IF NOT EXISTS JUDETE (COD VARCHAR(2) PRIMARY KEY,
                                              NUME VARCHAR(25) NOT NULL,
                                              POPULATIE INT NOT NULL)""",
        """CREATE TABLE IF NOT EXISTS POPULATIE (NUME VARCHAR(75) NOT NULL,
                                                 ADRESA VARCHAR(75) NOT NULL,
                                                 CNP BIGINT PRIMARY KEY,
                                                 COD_JUDET VARCHAR(2) NOT NULL)""",
        """CREATE TABLE IF NOT EXISTS CANDIDATI(serialNbr SERIAL,
                                                NUME VARCHAR(25) NOT NULL,
                                                PRIMARY KEY (NUME))""",
        """CREATE TABLE IF NOT EXISTS ZIUA_VOTARII(serialNbr SERIAL,
                                                   CNP BIGINT PRIMARY KEY,
                                                   VOT VARCHAR(25) NOT NULL,
                                                   Ora_Votarii TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP)""")


commands_views = """create view rezultate_per_judete as 
                     with 
                     t1 as (select distinct p.cod_judet, zv.vot , count(*) as rezultat
                            from populatie p
                            inner join ziua_votarii zv
                            on p.cnp = zv.cnp
                                group by 1, 2),
 
                     t2 as (select t1.cod_judet, max(t1.rezultat) as rezultat from t1 group by 1),
 
                     t3 as (select t1.cod_judet, t1.vot
                            from t1
                            inner join t2 on t1.cod_judet = t2.cod_judet and t1.rezultat = t2.rezultat), 
 
                     t4 as (select j.cod, j.nume, j.populatie, t3.vot as rezultat
                            from judete as j
                            left join t3
                                on j.cod = t3.cod_judet
                            group by 1, 2, 3, 4)
 
                     select * from t4 order by t4.nume asc"""

def file_exists(file_name, path=gdpath):
    """"""
    if os.path.exists(f"{gdpath}\\{file_name}.csv"):
        return pd.read_csv(f"{gdpath}\\{file_name}.csv")
    else:
        print(f"File {file_name}.csv is missing!!!")
    
    
df_judete = file_exists("judete")
df_populatie = file_exists("populatie")
df_candidati = file_exists("candidati")


if __name__ == "__main__":

    app = inserare_date(h, d, u, p)

    for i in ltables:
        app.send_query(f"drop table if exists {i}")
    app.create_table_view(commands_tables)

    for i in lviews:
        app.send_query(f"drop view  if exists {i}")
    app.send_query(commands_views)
    
    app.create_fk_constrait("populatie", "cod_judet", "judete", "cod")
    app.create_fk_constrait("ziua_votarii", "cnp", "populatie", "cnp")
    app.create_fk_constrait("ziua_votarii", "vot", "candidati", "nume")
    
    app.insert_into(df_judete, "judete", expand=False)
    app.insert_into(df_populatie, "populatie", batch_size=200000, expand=False)
    app.insert_into(df_candidati, "candidati", expand=False)
    
    

    app.con_close()
    
