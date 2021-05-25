import pandas as pd
import os
import psycopg2
import time
from pathlib import Path




class inserare_date():
    def __init__(self, _host, _database, _user, _password):
        self.host = _host
        self.database = _database
        self.user = _user
        self.password = _password
        
        self.connection = psycopg2.connect(host=self.host, database=self.database, user=self.user, password=self.password)
        self.cursor = self.connection.cursor()
        
    def send_query(self, query):
        """"""
        try:
            self.cursor.execute(query)
            self.connection.commit()
            print(f"Query `{query}` returned successfully")
        except Exception as e:
            print(e)
            self.connection.close()
    
    def create_table_view(self, query):
        """query : tuple"""
        try: 
            for i in query:
                self.cursor.execute(i)
#             self.cursor.close
            self.connection.commit()
        except (Exception, psycopg2.DatabaseError) as e:
            print(e)
            self.connection.close()
    
    def read_sql(self, table_name):
        """"""
        df = pd.read_sql(f"select * from {table_name}", con=self.connection)
        return df
    
    
    def create_fk_constrait(self, child_table, child_col, parent_table, parent_col):
        """"""
        try:
            self.cursor.execute(f"""ALTER TABLE {child_table} ADD CONSTRAINT fk_{child_col} FOREIGN KEY ({child_col}) REFERENCES {parent_table} ({parent_col});""")
            print(f"Constraint fk_{child_col} added!!!")
            self.connection.commit()
        except (Exception, psycopg2.DatabaseError) as e:
            print(e)
            self.connection.close()
    
    
    def insert_into(self, df, nume_tabela, batch_size=1, sleep_time=0, expand=False):
        """"""
        
        max_col = len(df.columns)
        values = "%s, "*max_col
        values = values[:-2]
        rows = list(tuple(i) for i in df.values)
        
        try: 
            for i in range(0, int(df.shape[0]), batch_size):
                lower = i
                upper = i + batch_size
                temp_batch = rows[lower:upper]

                self.cursor.execute(f"""SELECT COUNT(*) FROM {nume_tabela}""") if expand==True else None
                print(f"before: {self.cursor.fetchone()}") if expand==True else None

                self.cursor.executemany(f"""INSERT INTO {nume_tabela} VALUES ({values})""", temp_batch)
                self.connection.commit()

                self.cursor.execute(f"""SELECT COUNT(*) FROM {nume_tabela}""") if expand==True else None
                print(f"after:: {self.cursor.fetchone()}") if expand==True else None
                
            time.sleep(sleep_time)
        except (Exception, psycopg2.DatabaseError) as e:
            print(e)
            self.connection.close()
        
        
    def con_close(self):
        """"""
        try:
            if self.connection is not None:
                self.connection.close()
        except Exception as e:
            print(e)
        