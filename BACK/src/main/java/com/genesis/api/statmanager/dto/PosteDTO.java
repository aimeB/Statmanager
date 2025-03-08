package com.genesis.api.statmanager.dto;

import com.genesis.api.statmanager.model.enumeration.CategoriePoste;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PosteDTO {
    private String code; // Par exemple : "GB", "DC", "MDF"
    private String description; // Par exemple : "Gardien", "Défenseur Central", "Milieu Défensif"


}


