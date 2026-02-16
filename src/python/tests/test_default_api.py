from fastapi.testclient import TestClient


def test_create_lamp(client: TestClient):
    """Test case for create_lamp

    Create a new lamp
    """
    lamp_create = {"status": True}

    headers = {}
    response = client.request(
        "POST",
        "/v1/lamps",
        headers=headers,
        json=lamp_create,
    )

    assert response.status_code == 201


def test_delete_lamp(client: TestClient):
    """Test case for delete_lamp

    Delete a lamp
    """

    lamp_create = {"status": True}

    headers = {}
    response = client.request(
        "POST",
        "/v1/lamps",
        headers=headers,
        json=lamp_create,
    )

    headers = {}
    response = client.request(
        "DELETE",
        "/v1/lamps/{lampId}".format(lampId=response.json()["id"]),
        headers=headers,
    )

    assert response.status_code == 204


def test_get_lamp(client: TestClient):
    """Test case for get_lamp

    Get a specific lamp
    """

    lamp_create = {"status": True}

    headers = {}
    response = client.request(
        "POST",
        "/v1/lamps",
        headers=headers,
        json=lamp_create,
    )

    headers = {}
    response = client.request(
        "GET",
        "/v1/lamps/{lampId}".format(lampId=response.json()["id"]),
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
        "/v1/lamps",
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
        "/v1/lamps",
        headers=headers,
        json=lamp_create,
    )

    lamp_update = {"status": True}

    headers = {}
    response = client.request(
        "PUT",
        "/v1/lamps/{lampId}".format(lampId=response.json()["id"]),
        headers=headers,
        json=lamp_update,
    )

    assert response.status_code == 200


def test_list_lamps_pagination_progression(client: TestClient):
    """List endpoint should honor page size and cursor progression."""
    for i in range(30):
        response = client.request("POST", "/v1/lamps", json={"status": i % 2 == 0})
        assert response.status_code == 201

    first_page = client.request("GET", "/v1/lamps?pageSize=25")
    assert first_page.status_code == 200
    first_payload = first_page.json()
    assert len(first_payload["data"]) == 25
    assert first_payload["hasMore"] is True
    assert first_payload["nextCursor"] == "25"

    second_page = client.request("GET", "/v1/lamps?pageSize=25&cursor=25")
    assert second_page.status_code == 200
    second_payload = second_page.json()
    assert len(second_payload["data"]) == 5
    assert second_payload["hasMore"] is False
    assert second_payload["nextCursor"] is None


def test_list_lamps_invalid_cursor_defaults_to_first_page(client: TestClient):
    """Invalid cursor should be treated as first-page offset."""
    for _ in range(3):
        response = client.request("POST", "/v1/lamps", json={"status": True})
        assert response.status_code == 201

    first_page = client.request("GET", "/v1/lamps?pageSize=2")
    invalid_cursor_page = client.request("GET", "/v1/lamps?pageSize=2&cursor=not-an-int")

    assert invalid_cursor_page.status_code == 200
    assert invalid_cursor_page.json() == first_page.json()
