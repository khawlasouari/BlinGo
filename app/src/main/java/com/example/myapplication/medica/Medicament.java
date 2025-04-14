package com.example.myapplication.medica;

public class Medicament {
    private int id;
    private String nomMedicament;
    private String dose;
    private String heurePrise;

    // Constructeur vide
    public Medicament() {
    }

    // Constructeur avec tous les paramètres
    public Medicament(int id, String nomMedicament, String dose, String heurePrise) {
        this.id = id;
        this.nomMedicament = nomMedicament;
        this.dose = dose;
        this.heurePrise = heurePrise;
    }

    // Constructeur sans ID (utilisé lors de l'ajout)
    public Medicament(String nomMedicament, String dose, String heurePrise) {
        this.nomMedicament = nomMedicament;
        this.dose = dose;
        this.heurePrise = heurePrise;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nomMedicament;
    }

    public void setNom(String nomMedicament) {
        this.nomMedicament = nomMedicament;
    }

    public String getDose() {
        return dose;
    }

    public void setDose(String dose) {
        this.dose = dose;
    }

    public String getHeurePrise() {
        return heurePrise;
    }

    public void setHeurePrise(String heurePrise) {
        this.heurePrise = heurePrise;
    }
}
