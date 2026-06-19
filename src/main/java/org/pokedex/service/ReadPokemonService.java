package org.pokedex.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.pokedex.model.Pokemon;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReadPokemonService {

    //private String filePath = "src/main/resources/pokedex.json";;
    private ArrayList<Pokemon> listPokemon = new ArrayList<>();
    private final Map<Integer, Pokemon> pokemonById = new HashMap<>();
    private final Map<String, Pokemon> pokemonByName = new HashMap<>();          // key: lowercased name
    private final Map<String, List<Pokemon>> pokemonByType = new HashMap<>();    // key: lowercased type

    @PostConstruct
    public ArrayList<Pokemon> getPokemonList() {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            ClassPathResource resource = new ClassPathResource("pokedex.json");
            JsonNode rootNode = objectMapper.readTree(resource.getInputStream());

            extractPokemonList(rootNode);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return listPokemon;
    }

    private void extractPokemonList(JsonNode rootNode) {
        for(JsonNode pokemonNode: rootNode){
            int id = pokemonNode.get("id").asInt();
            String name = pokemonNode.get("name").get("english").asText();
            ArrayList<String> types = new ArrayList<>();
                for(JsonNode typeNode: pokemonNode.get("type")){
                    types.add(typeNode.asText());
                }

            String description = pokemonNode.get("description").asText();
            Pokemon pokemon = new Pokemon(id, name, types, description);
            listPokemon.add(pokemon);
            pokemonById.put(id, pokemon);
            pokemonByName.put(name.toLowerCase(), pokemon);
                for (String t : types) {
                    pokemonByType.computeIfAbsent(t.trim().toLowerCase(), k -> new ArrayList<>()).add(pokemon);
                }
        }
    }

    public List<Pokemon> findAllPokemon(){
        return listPokemon;
    }

    public Pokemon findPokemonByID(int id){
        return pokemonById.get(id);
    }

    public Pokemon findPokemonByName(String name){
        return pokemonByName.get(name.toLowerCase());
    }

    public List<Pokemon> findPokemonByType(String type){
        return pokemonByType.getOrDefault(type.toLowerCase(), List.of());
    }
}
