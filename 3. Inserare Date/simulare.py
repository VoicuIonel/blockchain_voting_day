import pandas as pd
import numpy as np
import os
import psycopg2
from pathlib import Path
from faker import Faker
from collections import OrderedDict
from datetime import datetime
import time
from insert_class import inserare_date
import random
import math
import getpass

fake = Faker()
min_hour = 7
max_hour = 21
sample_zi_de_votare = 0.62


h, d, u, p = "localhost", "staging", input("User:"), getpass.getpass('Password:')

def batch_size_sum_sample(n, total):

    dividers = sorted(random.sample(range(1, total), n - 1))
    return [a - b for a, b in zip(dividers + [total], [0] + dividers)]



if __name__ == "__main__":

    app = inserare_date(h, d, u, p)
    df = app.read_sql("populatie")
    df = df.sample(frac=sample_zi_de_votare, weights=df.groupby('cod_judet')['cod_judet'].transform('count'))
    
    df_vd = pd.DataFrame(data={'CNP': df['cnp'].to_list()})
    df_vd['serialnbr'] = [i+1 for i in range(df_vd.shape[0])]

    df_vd['vot'] = df_vd.apply(lambda x: fake.random_elements(elements=OrderedDict([('BJP', .18), 
                                                                                    ('INC', .17), 
                                                                                    ('BSP', .17), 
                                                                                    ('CMD', .10), 
                                                                                    ('IPV', .10), 
                                                                                    ('BIT', .02), 
                                                                                    ('UTC', .06), 
                                                                                    ('VOP', .08), 
                                                                                    ('KLM', .09), 
                                                                                    ('JAR', .03)]), 
                                                              length=1),  
                               axis=1)
    
    df_vd['vot'] = df_vd['vot'].astype(str).str.replace("[", "").str.replace("]", "")
    df_vd['vot'] = df_vd['vot'].astype(object).str.replace("'", "")

    df_vd = df_vd[['serialnbr', 'CNP', 'vot']]
    df_vd['ora_votarii'] = np.nan

    batch_size = batch_size_sum_sample(14, df_vd.shape[0])

    for _hour, _bs in zip(range(min_hour, max_hour), batch_size) :

        temp_df_vd = df_vd[df_vd['ora_votarii'].isnull()]
        temp_df_vd = temp_df_vd.sample(n=_bs)
        temp_df_vd['ora_votarii'] = temp_df_vd['ora_votarii'].apply(lambda x: fake.date_time_between_dates(datetime(2021, 4, 11, _hour, 0, 0), datetime(2021, 4, 11, _hour+1, 0, 0)))

        df_vd = pd.merge(df_vd, temp_df_vd, 
                         on='CNP', how='left')
        df_vd['ora_votarii'] = np.where(df_vd['ora_votarii_x'].isnull(), df_vd['ora_votarii_y'].astype('datetime64[ns]'), df_vd['ora_votarii_x'].astype('datetime64[ns]'))
        df_vd.rename(columns={'serialnbr_x': 'serialnbr',
                              'vot_x': 'vot'}, inplace=True)
        df_vd = df_vd[['serialnbr', 'CNP', 'vot', 'ora_votarii']]

        print(f"hour: {_hour} / batch size: {_bs}")
    
    
    app.send_query("""DELETE FROM ziua_votarii;""")

    for i in range(min_hour, max_hour):
        print(f"hour: {i}")
        df_vd_insert = df_vd[df_vd['ora_votarii'].apply(lambda x: x.hour)==i]
        app.insert_into(df_vd_insert, 'ziua_votarii', batch_size=1000, expand=True)

    #     time.sleep(10)
    
    app.con_close()
    


    
