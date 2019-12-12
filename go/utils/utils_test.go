package utils

import (
	"testing"
)

func TestGetUUID(t *testing.T) {
	uuid := NewUUID()
	AssertEqual(t, 16, len(uuid))
	AssertEqual(t, 36, len(uuid.String()))
	uuidString := GetUUID()
	AssertEqual(t, 32, len(uuidString))
}