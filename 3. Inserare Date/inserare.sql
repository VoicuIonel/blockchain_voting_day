-- Creare tabele
CREATE TABLE IF NOT EXISTS JUDETE (COD VARCHAR(2) PRIMARY KEY,
                                   NUME VARCHAR(25) NOT NULL,
                                   POPULATIE INT NOT NULL);
CREATE TABLE IF NOT EXISTS POPULATIE (NUME VARCHAR(75) NOT NULL,
                                      ADRESA VARCHAR(75) NOT NULL,
                                      CNP BIGINT PRIMARY KEY,
                                      COD_JUDET VARCHAR(2) NOT NULL);
CREATE TABLE IF NOT EXISTS CANDIDATI(serialNbr SERIAL,
                                     NUME VARCHAR(25) NOT NULL,
                                     PRIMARY KEY (NUME));
CREATE TABLE IF NOT EXISTS ZIUA_VOTARII(serialNbr SERIAL,
                                        CNP BIGINT NOT NULL,
                                        VOT VARCHAR(25) NOT NULL,
                                        Ora_Votarii TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                        PRIMARY KEY(CNP));
                                      
                                        
-- Creare constrait foreign key
ALTER TABLE populatie ADD CONSTRAINT fk_cod_judet FOREIGN KEY (cod_judet) REFERENCES judete (cod);
ALTER TABLE ziua_votarii ADD CONSTRAINT fgk_cnp FOREIGN KEY (cnp) REFERENCES populatie (cnp);
ALTER TABLE ziua_votarii ADD CONSTRAINT fk_vot FOREIGN KEY (vot) REFERENCES candidati (nume);

create view rezultate_per_judete as 
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

	select * from t4 order by t4.nume asc




-- Inserarea datelor si crearea de tabele/viewuri a fost facuta din python



