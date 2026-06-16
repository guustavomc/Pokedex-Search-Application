package org.pokedex.controller;


import org.pokedex.exception.PokemonNotFoundException;
import org.pokedex.model.Pokemon;
import org.pokedex.service.ReadPokemonService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/pokemon")
public class PokedexController {

    private ReadPokemonService service;

    public PokedexController(ReadPokemonService service) {

        this.service = service;
    }

    @GetMapping
    public List<Pokemon> getAllPokemon(){
        return service.findAllPokemon();
    }

    @GetMapping("/id/{id}")
    public Pokemon getPokemonByID(@PathVariable("id") int id){
        Pokemon pokemon = service.findPokemonByID(service.findAllPokemon(), id);
        if (pokemon == null) throw new PokemonNotFoundException("No Pokemon found with id: " + id);
        return pokemon;
    }

    @GetMapping("/search/{search}")
    public Pokemon getPokemonByName(@PathVariable("search") String name){
        Pokemon pokemon = service.findPokemonByName(service.findAllPokemon(), name);
        if (pokemon == null) throw new PokemonNotFoundException("No Pokemon found with name: " + name);
        return pokemon;
    }

    @GetMapping("/type/{type}")
    public List<Pokemon> getPokemonByType(@PathVariable("type") String type){
        return service.findPokemonByType(type);

    }
}
