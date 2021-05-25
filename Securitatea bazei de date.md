# Securiatea bazei de datele

Politica de securitate a unei baze de date presupune in primul rand definirea de conturi de utilizare si atribuirea de drepturi de acces. Pentru acest proiect vom defini 2 useri dupa cum urmeaza:
- user1 --> va avea drepturi de SELECT, INSERT, UPDATE, DELETE la baza de date, va creata tabelele si va insera datele initiale
```
	create role user1 with login password 'password1' valid until '2021-05-29 00:00:00.533249+00';
	
	GRANT CONNECT ON DATABASE staging TO user1;
	GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA staging TO user1;
		
```
	
- user2 --> va avea drepturi doar de SELECT din baza pentru a crea conexiunea dintre Microsoft PowerBI si baza PostgreSQL
	
```
	create role user2 with login password 'password2' valid until '2021-05-29 00:00:00.533249+00';
	GRANT CONNECT ON DATABASE staging TO user2;
	
	GRANT SELECT ON candidati, judete, populatie, ziua_votarii, rezultate_per_judete TO user2;
	
```
	
In acelasi timp, vom limita accesul la baza de date prin adaugarea stricta a IP-urilor. Acest lucru se va face prin fisierul [pg_hba.conf], unde putem limita si userii care se pot conectata.


![pg_hba](https://user-images.githubusercontent.com/54807691/117874322-7f4e7f00-b2a9-11eb-8d7c-4afcfa8268c6.png)




