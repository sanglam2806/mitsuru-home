import logging
import pandas as pd
from mimesis import Fieldset
from mimesis.locales import Locale
from db import connect_db

logging.basicConfig(format="%(asctime)s %(levelname)s: %(message)s", level=logging.INFO)

def gen_accounts(locale, quantity):
    fs = Fieldset(locale, i=quantity)
    df = pd.DataFrame.from_dict({
        "account_id": fs("uuid"),
        "user_name": fs("person.full_name"),
        "email": fs("email"),
        "phone": fs("telephone", mask="+84 (###) ###-###"),
        "birthdate": fs("datetime",start=1960, end=2012),
        "create_at": fs("datetime",start=2024, end=2025),
        "update_at": fs("datetime",start=2024, end=2025),
        })

    # output = json.dumps(df.to_dict("records"), indent = 4)

    output = df.to_dict("records")
    return output

if __name__ ==  "__main__":
    accounts = gen_accounts(Locale.EN, 10000)
    dbconnect = connect_db()
    if dbconnect != None :
        accounts_collection = dbconnect["accounts"];
        logging.info("Adding data start!");
        accounts_collection.insert_many(accounts);
        logging.info("Work Done!");
    else :
        logging.error("cannot connect to DB")
