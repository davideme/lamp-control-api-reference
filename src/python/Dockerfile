FROM python:3.12

WORKDIR /code

COPY ./requirements.txt /code/requirements.txt

RUN pip install --no-cache-dir --upgrade --trusted-host pypi.org --trusted-host pypi.python.org --trusted-host files.pythonhosted.org -r /code/requirements.txt

COPY ./app /code/app
COPY ./src /code/src

CMD ["fastapi", "run", "app/main.py", "--proxy-headers", "--port", "80"]
