package main

import "testing"

func TestNormalizeVille(t *testing.T) {
	tests := []struct {
		input    string
		expected string
	}{
		{"Clermont-Ferrand", "clermont"},
		{"Aubiere", "aubiere"},
		{"Chamalieres", "chamalieres"},
		{"VilleInconnue", "VilleInconnue"},
	}

	for _, test := range tests {
		result := normalizeVille(test.input)
		if result != test.expected {
			t.Errorf("Pour %s, attendu %s, mais obtenu %s", test.input, test.expected, result)
		}
	}
}