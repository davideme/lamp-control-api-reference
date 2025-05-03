from fastapi.testclient import TestClient


def test_create_lamp(client: TestClient):
    """Test case for create_lamp

    Create a new lamp
    """
    lamp_create = {"status": True}

    headers = {}
    response = client.request(
        "POST",
        "/lamps",
        headers=headers,
        json=lamp_create,
    )

    assert response.status_code == 200


def test_delete_lamp(client: TestClient):
    """Test case for delete_lamp

    Delete a lamp
    """

    lamp_create = {"status": True}

    headers = {}
    response = client.request(
        "POST",
        "/lamps",
        headers=headers,
        json=lamp_create,
    )

    headers = {}
    response = client.request(
        "DELETE",
        "/lamps/{lampId}".format(lampId=response.json()["id"]),
        headers=headers,
    )

    assert response.status_code == 200


def test_get_lamp(client: TestClient):
    """Test case for get_lamp

    Get a specific lamp
    """

    headers = {}
    response = client.request(
        "GET",
        "/lamps/{lampId}".format(lampId="lamp_id_example"),
        headers=headers,
    )

    assert response.status_code == 200


def test_list_lamps(client: TestClient):
    """Test case for list_lamps

    List all lamps
    """

    headers = {}
    response = client.request(
        "GET",
        "/lamps",
        headers=headers,
    )

    assert response.status_code == 200


def test_update_lamp(client: TestClient):
    """Test case for update_lamp

    Update a lamp's status
    """
    lamp_create = {"status": False}

    headers = {}
    response = client.request(
        "POST",
        "/lamps",
        headers=headers,
        json=lamp_create,
    )

    lamp_update = {"status": True}

    headers = {}
    response = client.request(
        "PUT",
        "/lamps/{lampId}".format(lampId=response.json()["id"]),
        headers=headers,
        json=lamp_update,
    )

    assert response.status_code == 200
