from faker import Faker
import pandas as pd
import os
pd.set_option('display.float_format', '{:.2f}'.format)

fake = Faker(['ro-RO'])
dpath = os.getcwd()

try:
    dfc = pd.read_csv('temp_judete.csv')
except Exception as e:
    print(e)
    
df_pop = pd.DataFrame(columns=['Nume', 'Adresa', 'CNP', 'Cod Oras'])


# Populatie
for i in range(dfc.shape[0]):
    nume, adresa, cnp, cod_oras = [], [], [], []

    for j in range(dfc.iloc[i][2]):
#         Faker.seed(j) # o sa avem de fiecare data acelasi rezultat

        nume.append(fake.name())
        adresa.append(fake.address().split('\n')[0])

        y = fake.random_int(0, 99) 
        m = fake.random_int(1, 12) 
        d = fake.random_int(1, 31) 
        r = fake.random_int(0, 999999) 

        cnp.append(''.join(str(i) for i in [fake.random_int(1,2) if fake.random_int(1900,2099)<2000 else fake.random_int(5,6), 
                                            f'0{y}' if len(str(y))==1 else y,
                                            f'0{m}' if len(str(m))==1 else m,
                                            f'0{d}' if len(str(d))==1 else d,
                                            str(r).zfill(6) if len(str(r))<6 else r]))
    temp_df = temp_df_pop = pd.DataFrame(data={'Nume': nume, 'Adresa': adresa, 'CNP': cnp})
    temp_df['Cod Oras'] = dfc.iloc[i][0]

    df_pop = pd.concat([df_pop, temp_df])
    print(f"{i}. judet = {dfc.iloc[i][1]} // populatia = {dfc.iloc[i][2]}")

df_pop.drop_duplicates(subset=['CNP'], inplace=True)    
df_pop.to_csv(f'{dpath}\populatie.csv', index=False, encoding='utf-8')


# Ne asiguram ca numarul populatiei din tabelul judete coincide cu cel din populatie, 
# pentru ca exista o mica posibilitate ca cnp-ul generat din populatie sa nu fie unic
df_pop_check = df_pop.groupby(by=['Cod Oras']).count()
df_pop_check.reset_index(inplace=True)
df_pop_check.rename(columns={"Nume": "Populatie"}, inplace=True)

df_judete = dfc.merge(df_pop_check[['Cod Oras', 'Populatie']], left_on='countyCode', right_on="Cod Oras")
df_judete = df_judete[['countyCode', 'countyNme', "Populatie_y"]]
df_judete.rename(columns={'countyCode': "Cod Oras",
                          "countyNme": "Nume",
                          "Populatie_y": "Populatie"}, inplace=True)

df_judete.to_csv(f'{dpath}\judete.csv', index=False)

# candidati
lnumdCand = ['BJP', 'INC', 'BSP', 'CMD', 'IPV', 'BIT', 'UTC', 'VOP', 'KLM', 'JAR']
lidCand = [1000000001, 1000000002, 1000000003, 1000000004, 1000000005, 1000000006, 1000000007, 1000000008, 1000000009, 1000000010]

df_cand = pd.DataFrame(data={'ID': lidCand,'Nume': lnumdCand})
df_cand.to_csv(f"{dpath}\candidati.csv", index=False)


# ziua_votului
df_vot = pd.DataFrame(columns=['CNP', 'Vot', 'Ora Votarii'])
df_vot.to_csv(f"{dpath}\\ziua_votarii.csv", index=False)
