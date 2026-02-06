"""Tests for generated Pydantic models.

Tests cover serialization methods (to_str, to_json, to_dict, from_dict, from_json)
and edge cases like None input and non-dict input for from_dict.
"""

from datetime import UTC, datetime

import pytest

from src.openapi_server.models.error import Error
from src.openapi_server.models.lamp import Lamp
from src.openapi_server.models.lamp_create import LampCreate
from src.openapi_server.models.lamp_update import LampUpdate
from src.openapi_server.models.list_lamps200_response import ListLamps200Response


class TestErrorModel:
    """Tests for the Error model."""

    def test_to_str(self):
        """to_str should return a string representation."""
        error = Error(error="NOT_FOUND")
        result = error.to_str()
        assert "NOT_FOUND" in result

    def test_to_json(self):
        """to_json should return valid JSON."""
        error = Error(error="NOT_FOUND")
        result = error.to_json()
        assert '"error"' in result
        assert "NOT_FOUND" in result

    def test_to_dict(self):
        """to_dict should return a dictionary."""
        error = Error(error="INVALID_ARGUMENT")
        result = error.to_dict()
        assert result == {"error": "INVALID_ARGUMENT"}

    def test_from_dict(self):
        """from_dict should create an Error from a dictionary."""
        error = Error.from_dict({"error": "NOT_FOUND"})
        assert error.error == "NOT_FOUND"

    def test_from_dict_none(self):
        """from_dict with None should return None."""
        result = Error.from_dict(None)
        assert result is None

    def test_from_dict_non_dict(self):
        """from_dict with non-dict should use model_validate."""
        error = Error(error="TEST")
        result = Error.from_dict(error)
        assert result.error == "TEST"

    def test_from_json(self):
        """from_json should create an Error from a JSON string."""
        error = Error.from_json('{"error": "NOT_FOUND"}')
        assert error.error == "NOT_FOUND"


class TestLampCreateModel:
    """Tests for the LampCreate model."""

    def test_to_str(self):
        """to_str should return a string representation."""
        lamp = LampCreate(status=True)
        result = lamp.to_str()
        assert "True" in result

    def test_to_json(self):
        """to_json should return valid JSON."""
        lamp = LampCreate(status=False)
        result = lamp.to_json()
        assert '"status"' in result

    def test_to_dict(self):
        """to_dict should return a dictionary."""
        lamp = LampCreate(status=True)
        result = lamp.to_dict()
        assert result == {"status": True}

    def test_from_dict(self):
        """from_dict should create a LampCreate from a dictionary."""
        lamp = LampCreate.from_dict({"status": True})
        assert lamp.status is True

    def test_from_dict_none(self):
        """from_dict with None should return None."""
        result = LampCreate.from_dict(None)
        assert result is None

    def test_from_dict_non_dict(self):
        """from_dict with non-dict should use model_validate."""
        original = LampCreate(status=True)
        result = LampCreate.from_dict(original)
        assert result.status is True

    def test_from_json(self):
        """from_json should create a LampCreate from a JSON string."""
        lamp = LampCreate.from_json('{"status": true}')
        assert lamp.status is True


class TestLampUpdateModel:
    """Tests for the LampUpdate model."""

    def test_to_str(self):
        """to_str should return a string representation."""
        lamp = LampUpdate(status=False)
        result = lamp.to_str()
        assert "False" in result

    def test_to_json(self):
        """to_json should return valid JSON."""
        lamp = LampUpdate(status=True)
        result = lamp.to_json()
        assert '"status"' in result

    def test_to_dict(self):
        """to_dict should return a dictionary."""
        lamp = LampUpdate(status=False)
        result = lamp.to_dict()
        assert result == {"status": False}

    def test_from_dict(self):
        """from_dict should create a LampUpdate from a dictionary."""
        lamp = LampUpdate.from_dict({"status": False})
        assert lamp.status is False

    def test_from_dict_none(self):
        """from_dict with None should return None."""
        result = LampUpdate.from_dict(None)
        assert result is None

    def test_from_dict_non_dict(self):
        """from_dict with non-dict should use model_validate."""
        original = LampUpdate(status=False)
        result = LampUpdate.from_dict(original)
        assert result.status is False

    def test_from_json(self):
        """from_json should create a LampUpdate from a JSON string."""
        lamp = LampUpdate.from_json('{"status": false}')
        assert lamp.status is False


class TestLampModel:
    """Tests for the Lamp model."""

    def _make_lamp(self, **kwargs):
        """Helper to create a Lamp with default values."""
        defaults = {
            "id": "lamp-1",
            "status": True,
            "createdAt": datetime(2024, 1, 1, tzinfo=UTC),
            "updatedAt": datetime(2024, 1, 2, tzinfo=UTC),
        }
        defaults.update(kwargs)
        return Lamp(**defaults)

    def test_to_str(self):
        """to_str should return a string representation."""
        lamp = self._make_lamp()
        result = lamp.to_str()
        assert "lamp-1" in result

    def test_to_json_raises_on_datetime(self):
        """to_json raises TypeError since generated code uses json.dumps on datetime objects."""
        lamp = self._make_lamp()
        with pytest.raises(TypeError, match="not JSON serializable"):
            lamp.to_json()

    def test_to_dict(self):
        """to_dict should return a dictionary with aliases."""
        lamp = self._make_lamp()
        result = lamp.to_dict()
        assert result["id"] == "lamp-1"
        assert result["status"] is True
        assert "createdAt" in result
        assert "updatedAt" in result

    def test_from_dict(self):
        """from_dict should create a Lamp from a dictionary."""
        data = {
            "id": "lamp-2",
            "status": False,
            "createdAt": "2024-01-01T00:00:00Z",
            "updatedAt": "2024-01-02T00:00:00Z",
        }
        lamp = Lamp.from_dict(data)
        assert lamp.id == "lamp-2"
        assert lamp.status is False

    def test_from_dict_none(self):
        """from_dict with None should return None."""
        result = Lamp.from_dict(None)
        assert result is None

    def test_from_dict_non_dict(self):
        """from_dict with non-dict should use model_validate."""
        original = self._make_lamp()
        result = Lamp.from_dict(original)
        assert result.id == "lamp-1"

    def test_from_json(self):
        """from_json should create a Lamp from a JSON string."""
        json_str = (
            '{"id": "l1", "status": true,'
            ' "createdAt": "2024-01-01T00:00:00Z",'
            ' "updatedAt": "2024-01-01T00:00:00Z"}'
        )
        lamp = Lamp.from_json(json_str)
        assert lamp.id == "l1"


class TestListLamps200ResponseModel:
    """Tests for the ListLamps200Response model."""

    def _make_lamp_dict(self, id="lamp-1", status=True):
        return {
            "id": id,
            "status": status,
            "createdAt": "2024-01-01T00:00:00Z",
            "updatedAt": "2024-01-01T00:00:00Z",
        }

    def _make_lamp(self, id="lamp-1", status=True):
        return Lamp(
            id=id,
            status=status,
            createdAt=datetime(2024, 1, 1, tzinfo=UTC),
            updatedAt=datetime(2024, 1, 1, tzinfo=UTC),
        )

    def test_to_str(self):
        """to_str should return a string representation."""
        response = ListLamps200Response(
            data=[self._make_lamp()],
            hasMore=False,
        )
        result = response.to_str()
        assert "lamp-1" in result

    def test_to_json_raises_on_datetime(self):
        """to_json raises TypeError since nested Lamp objects contain datetime fields."""
        response = ListLamps200Response(
            data=[self._make_lamp()],
            hasMore=False,
        )
        with pytest.raises(TypeError, match="not JSON serializable"):
            response.to_json()

    def test_to_dict_with_data(self):
        """to_dict should serialize nested Lamp objects."""
        response = ListLamps200Response(
            data=[self._make_lamp()],
            hasMore=False,
        )
        result = response.to_dict()
        assert len(result["data"]) == 1
        assert result["data"][0]["id"] == "lamp-1"
        assert result["hasMore"] is False

    def test_to_dict_with_null_next_cursor(self):
        """to_dict should include nextCursor as None when explicitly set."""
        # Use model_validate so model_fields_set includes next_cursor
        response = ListLamps200Response.model_validate(
            {"data": [], "nextCursor": None, "hasMore": False}
        )
        result = response.to_dict()
        assert "nextCursor" in result
        assert result["nextCursor"] is None

    def test_to_dict_with_next_cursor(self):
        """to_dict should include nextCursor when set."""
        response = ListLamps200Response(
            data=[],
            nextCursor="cursor-abc",
            hasMore=True,
        )
        result = response.to_dict()
        assert result["nextCursor"] == "cursor-abc"
        assert result["hasMore"] is True

    def test_to_dict_empty_data(self):
        """to_dict with empty data list."""
        response = ListLamps200Response(data=[], hasMore=False)
        result = response.to_dict()
        assert result["data"] == []

    def test_from_dict(self):
        """from_dict should create a ListLamps200Response from a dictionary."""
        data = {
            "data": [self._make_lamp_dict()],
            "nextCursor": "abc",
            "hasMore": True,
        }
        response = ListLamps200Response.from_dict(data)
        assert len(response.data) == 1
        assert response.data[0].id == "lamp-1"
        assert response.next_cursor == "abc"
        assert response.has_more is True

    def test_from_dict_none(self):
        """from_dict with None should return None."""
        result = ListLamps200Response.from_dict(None)
        assert result is None

    def test_from_dict_non_dict(self):
        """from_dict with non-dict should use model_validate."""
        original = ListLamps200Response(
            data=[self._make_lamp()],
            hasMore=False,
        )
        result = ListLamps200Response.from_dict(original)
        assert len(result.data) == 1

    def test_from_dict_with_no_data_raises_validation_error(self):
        """from_dict with None data raises validation error since data is a required list."""
        from pydantic import ValidationError

        data = {"data": None, "hasMore": False}
        with pytest.raises(ValidationError):
            ListLamps200Response.from_dict(data)

    def test_from_json(self):
        """from_json should create from a JSON string."""
        json_str = (
            '{"data": [{"id": "l1", "status": true,'
            ' "createdAt": "2024-01-01T00:00:00Z",'
            ' "updatedAt": "2024-01-01T00:00:00Z"}],'
            ' "hasMore": false}'
        )
        response = ListLamps200Response.from_json(json_str)
        assert len(response.data) == 1
