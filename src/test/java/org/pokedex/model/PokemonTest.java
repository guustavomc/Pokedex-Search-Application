package org.pokedex.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PokemonTest {

    @Test
    void constructor_setsAllFields() {
        List<String> types = Arrays.asList("Grass", "Poison");
        Pokemon pokemon = new Pokemon(1, "Bulbasaur", types, "A strange seed was planted on its back at birth.");

        assertThat(pokemon.getId()).isEqualTo(1);
        assertThat(pokemon.getName()).isEqualTo("Bulbasaur");
        assertThat(pokemon.getType()).containsExactly("Grass", "Poison");
        assertThat(pokemon.getDescription()).isEqualTo("A strange seed was planted on its back at birth.");
    }

    @Test
    void setters_updateFields() {
        Pokemon pokemon = new Pokemon(1, "Bulbasaur", Arrays.asList("Grass"), "original desc");

        pokemon.setId(2);
        pokemon.setName("Ivysaur");
        pokemon.setType(Arrays.asList("Grass", "Poison"));
        pokemon.setDescription("Updated description.");

        assertThat(pokemon.getId()).isEqualTo(2);
        assertThat(pokemon.getName()).isEqualTo("Ivysaur");
        assertThat(pokemon.getType()).containsExactly("Grass", "Poison");
        assertThat(pokemon.getDescription()).isEqualTo("Updated description.");
    }

    @Test
    void pokemonDescription_containsNameIdTypeAndDescription() {
        Pokemon pokemon = new Pokemon(1, "Bulbasaur", Arrays.asList("Grass", "Poison"), "A strange seed.");
        String desc = pokemon.pokemonDescription();

        assertThat(desc).contains("Bulbasaur");
        assertThat(desc).contains("1");
        assertThat(desc).contains("Grass");
        assertThat(desc).contains("Poison");
        assertThat(desc).contains("A strange seed.");
    }
}
