import os
import time

path = os.path.dirname(os.path.abspath(__file__))
os.chdir(path)

os.system("docker compose up -d")
time.sleep(5)

os.system("docker compose exec broker \
    kafka-topics --create --topic logs \
    --bootstrap-server localhost:9092 \
    --replication-factor 1 --partitions 1")

os.system("docker compose -f rest-proxy.yml up -d")