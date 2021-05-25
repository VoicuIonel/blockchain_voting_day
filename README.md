# blockchain_voting_day
Sistem de votare bazat pe blockchain


## Aplicatii folosite in aplicatie:
	- [NetBeans 12.0] (https://netbeans.apache.org/download/nb120/nb120.html)
	- [Jupyter Notebook] (https://www.anaconda.com/)
	- [PostgreSQL] (https://www.postgresql.org/download/)
	- [Microsof Power BI] (se va instala direct din Microsoft Store)
	
	
## Structurarea proiectului
	- [1. Blockchain]
	- [2. Generare Date]
	- [3. Inserare Date]
	- [4. Dashboard]

## Note
Fiecare folder va contine un fisier .ipynb si cat si unul .py. Ele nu sunt diferite cu absolut nimic. Au fost create pentru o mai buna vizualizare a codului pentru cei ce nu au `Jupyter Notebook` instalat. 

	
### [1. Blockchain]

Sistemul de votare a fost dezvoltat in limbajul JAVA.
Sectiunea este formata din 7 clase de Java:

[Block.java] s-a implementat o clasă interioară numită Vote care este alcătuită din variabilele: voterId, voterName și voteParty. Aici fiecare candidat are un id, un nume și la final se va realiza în funcție de rezultatul obținut după votare, un vot de petrecere. Variabila previousHash marchează hash-ul anterior și variabila blockHash marchează blocul de hash. 

[ClientManager.java] am marcat canalul de comunicație care comunică cu serverul și pe baza a celor trei algoritmi: BJP, INC și BSP pot să adaug o criptare în block-ul generic. Dacă nu se poate conecta la server se va afișa un mesaj de eroare, în sensul că clientul „nu se poate conecta la server”. Există o funcție „startClient” care ne întâmpină cu un mesaj „Bine ați venit la mașina de vot!”. Pentru a continua procesul de votare trebuie să tastez pe y. După acest lucru, afișez un mesaj „Introduceți ID-ul alegătorului:”, apoi se afișează un mesaj  "Introduceți numele alegătorului" și la final se afișează un mesaj „Vot pentru petreceri:”. După ce se introduce votul, îl rog pe votant printr-un mesaj să introducă corect indexul pentru fiecare candidat.  Dacă nu respectă aceste instrucțiuni se va afișa un mesaj care indică faptul că votul este nevalid. Dacă dorește să pună alt vot se poate realiza acest lucru. Se creează un cifru folosindu-se de algoritmii: AES, ECB și PKCS5Padding. Există un obiect de decriptare folosindu-se de aceeași algoritmi.  Este o funcție ce verifică validitatea block-ului. Este o funcție ce trimite un mesaj către clienți. După ce se realizează aceste instrucțiuni informațiile se stochează într-un folder numit „blockchain_data” fiind situat pe Desktop.
[Main.java]  am declarat adresa serverului care este „localhost” și portul acestuia este 6777. Prin mai multe mesaje am afișat meniul principal și prezint un anumit ajutor dacă anumiți votanți se încurcă la selectarea votului sau la utilizarea aplicației mele.
[MesageStruct.java] --> am realizat o structură pentru comunicarea între server și client. Tot aici prezint 3 tipuri de mesaje și descrierea acestora.
[NetworkManager.java] --> furnizez operațiunea de rețea. Am creat o funcție în care trimit un mesaj la canalul de comunicație. Prin funcția „receiveMsg” se încearcă să primească un mesaj de la socket. 
[ServerHandler.java] --> am pregătit și am așteptat mesajele de la un client  specificat cu by_socket. 
[ServerManager.java] --> sunt marcate toate comunicațiile de rețea din partea serverului.  Sunt gestionate ID-urile clienților și se păstrează o hartă între ID-ul clientului și canalul comunicației unui client. Tot aici se pot conecta clienți noi.  
	
	
### [2. Generare Date]
# IMPORTANT NOTE ::: Datele generate sunt pur fictive 

Toate datele folosite in aplicatie vor fi generate utilizand libraria Faker. Inainte de a incepe ne asiguram ca toate librariile sunt instalate. 

```python
pip install Faker
pip install pandas
pip install psycopg2
pip install pathlib
```

Ca si punct de start vom avea fisierul `temp_judete.csv`, care va contine toate judetele din Romania si populatia fiecarui judet. Pe baza acestui fisier se vor genera datele pentru populatie. Din moment ce numerele sunt generate aleator, va exista posibilitatea ca `CNP`-ul sa se repete si din cauza asta am sters duplicatele. Pentru a fi 100% siguri ca datele din tabelul `JUDETE` coincid cu cle din `POPULATIE` am creat un nou fisier pentru judete in care am am luat datele din populatie.  
Dupa ce s-au generat datele, ele vor fi exportate in fisier .csv. 


### [3. Inserare Date]
Fisierul principal este `inserare.ipynb`, unde se va face conexiunea cu server-ul, PostgreSQL, se vor crea 4 tabele (`JUDETE`, `POPULATIE`, `CANDIDATI` si `ZIUA_VOTARII`, si se vor insera datele ce au fost create. Fisierul va apela clasa `inserare_date` care a fost creata separat. 

Pentru a se simuala o zi de votare, s-a creat fisierul `simulare.ipynb` unde un esantion de 62% din toata populatia Romaniei a fost creat. Acest esantion s-a impartit in mod aleator in 14 (numarul orelor in care populatia are dreptul sa voteze, de la 07:00 - 21:00) si au fost inserate pe tot parcursul zilei.  


### [4. Dashboard]
Pentru a rula este nevoie de versiune de Microsoft PowerBI Desktop care este free si se poate descarca direct din Microsoft Store


### Bibliografie
[ClientManager.java, ServerHandler.java, ServerManager.java] --> codul sursa este bazat pe noțiunile învățate în acest semestru de la disciplina „Programare paralelă, concurentă și distribuită”.













