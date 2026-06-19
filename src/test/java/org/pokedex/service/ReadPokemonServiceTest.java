package org.pokedex.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pokedex.model.Pokemon;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ReadPokemonServiceTest {

    private ReadPokemonService service;
    private List<Pokemon> testPokedex;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        service = new ReadPokemonService();

        testPokedex = Arrays.asList(
                new Pokemon(1, "Bulbasaur", Arrays.asList("Grass", "Poison"), "A strange seed was planted on its back at birth."),
                new Pokemon(4, "Charmander", Arrays.asList("Fire"), "Obviously prefers hot places."),
                new Pokemon(25, "Pikachu", Arrays.asList("Electric"), "When several gather, lightning storms occur.")
        );

        Map<Integer, Pokemon> byId = new HashMap<>();
        Map<String, Pokemon> byName = new HashMap<>();
        Map<String, List<Pokemon>> byType = new HashMap<>();
        for (Pokemon p : testPokedex) {
            byId.put(p.getId(), p);
            byName.put(p.getName().toLowerCase(), p);
            for (String t : p.getType()) {
                byType.computeIfAbsent(t.trim().toLowerCase(), k -> new ArrayList<>()).add(p);
            }
        }

        setField("listPokemon", new ArrayList<>(testPokedex));
        setField("pokemonById", byId);
        setField("pokemonByName", byName);
        setField("pokemonByType", byType);
    }

    private void setField(String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = ReadPokemonService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(service, value);
    }

    @Test
    void findAllPokemon_returnsAllPokemon() {
        assertThat(service.findAllPokemon()).hasSize(3);
    }

    @Test
    void findPokemonByID_whenFound_returnsPokemon() {
        Pokemon result = service.findPokemonByID(4);
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Charmander");
    }

    @Test
    void findPokemonByID_whenNotFound_returnsNull() {
        assertThat(service.findPokemonByID(999)).isNull();
    }

    @Test
    void findPokemonByName_whenFound_returnsPokemon() {
        Pokemon result = service.findPokemonByName("Pikachu");
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(25);
    }

    @Test
    void findPokemonByName_isCaseInsensitive() {
        Pokemon result = service.findPokemonByName("pikachu");
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Pikachu");
    }

    @Test
    void findPokemonByName_whenNotFound_returnsNull() {
        assertThat(service.findPokemonByName("Mewtwo")).isNull();
    }

    @Test
    void findPokemonByType_whenFound_returnsMatchingPokemon() {
        List<Pokemon> result = service.findPokemonByType("Grass");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Bulbasaur");
    }

    @Test
    void findPokemonByType_isCaseInsensitive() {
        List<Pokemon> result = service.findPokemonByType("fire");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Charmander");
    }

    @Test
    void findPokemonByType_matchesSecondaryType() {
        List<Pokemon> result = service.findPokemonByType("Poison");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Bulbasaur");
    }

    @Test
    void findPokemonByType_whenNotFound_returnsEmptyList() {
        assertThat(service.findPokemonByType("Dragon")).isEmpty();
    }
}
