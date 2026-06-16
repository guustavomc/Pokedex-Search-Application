package org.pokedex.controller;

import org.junit.jupiter.api.Test;
import org.pokedex.exception.GlobalExceptionHandler;
import org.pokedex.model.Pokemon;
import org.pokedex.service.ReadPokemonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PokedexController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class))
public class PokedexControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReadPokemonService service;

    private final Pokemon bulbasaur = new Pokemon(1, "Bulbasaur", Arrays.asList("Grass", "Poison"), "A strange seed was planted on its back at birth.");
    private final Pokemon charmander = new Pokemon(4, "Charmander", Arrays.asList("Fire"), "Obviously prefers hot places.");

    @Test
    void getAllPokemon_returnsFullList() throws Exception {
        when(service.findAllPokemon()).thenReturn(Arrays.asList(bulbasaur, charmander));

        mockMvc.perform(get("/api/pokemon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Bulbasaur"))
                .andExpect(jsonPath("$[1].name").value("Charmander"));
    }

    @Test
    void getPokemonByID_whenFound_returnsPokemon() throws Exception {
        when(service.findAllPokemon()).thenReturn(List.of(bulbasaur));
        when(service.findPokemonByID(anyList(), eq(1))).thenReturn(bulbasaur);

        mockMvc.perform(get("/api/pokemon/id/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Bulbasaur"))
                .andExpect(jsonPath("$.type[0]").value("Grass"))
                .andExpect(jsonPath("$.type[1]").value("Poison"));
    }

    @Test
    void getPokemonByID_whenNotFound_returnsNullBody() throws Exception {
        when(service.findAllPokemon()).thenReturn(List.of(bulbasaur));
        when(service.findPokemonByID(anyList(), eq(999))).thenReturn(null);

        mockMvc.perform(get("/api/pokemon/id/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("No Pokemon found with id: 999"));
    }

    @Test
    void getPokemonByName_whenFound_returnsPokemon() throws Exception {
        when(service.findAllPokemon()).thenReturn(List.of(bulbasaur));
        when(service.findPokemonByName(anyList(), eq("Bulbasaur"))).thenReturn(bulbasaur);

        mockMvc.perform(get("/api/pokemon/search/Bulbasaur"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bulbasaur"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getPokemonByName_whenNotFound_returnsNullBody() throws Exception {
        when(service.findAllPokemon()).thenReturn(List.of(bulbasaur));
        when(service.findPokemonByName(anyList(), eq("Unknown"))).thenReturn(null);

        mockMvc.perform(get("/api/pokemon/search/Unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("No Pokemon found with name: Unknown"));
    }

    @Test
    void getPokemonByType_whenFound_returnsMatchingList() throws Exception {
        when(service.findPokemonByType("Fire")).thenReturn(List.of(charmander));

        mockMvc.perform(get("/api/pokemon/type/Fire"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Charmander"))
                .andExpect(jsonPath("$[0].type[0]").value("Fire"));
    }

    @Test
    void getPokemonByType_whenNotFound_returnsEmptyList() throws Exception {
        when(service.findPokemonByType("Dragon")).thenReturn(List.of());

        mockMvc.perform(get("/api/pokemon/type/Dragon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
