import logging
from pymongo import MongoClient

def connect_db():
    connect_url = "mongodb://localhost:27017/?directConnection=true";
    try:
        client = MongoClient(connect_url)
    except Exception as e:
        logging.error("Cannot connect to db");
        logging.error(e.__str__())
        return None
    
    return client["mitsuru-home"];
