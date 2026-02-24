import itertools
lines=open('docker-compose.dev.yml').read().splitlines()
for idx,line in enumerate(lines[30:80], start=31):
    print(f'{idx:03}: {line}')
