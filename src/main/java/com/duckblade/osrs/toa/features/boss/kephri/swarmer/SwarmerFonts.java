package com.duckblade.osrs.toa.features.boss.kephri.swarmer;

import lombok.Getter;

@Getter
public enum SwarmerFonts {
    REGULAR("RS Regular"),
    ARIAL("Arial"),
    CAMBRIA("Cambria"),
    ROCKWELL("Rockwell"),
    SEGOE_UI("Segoe Ui"),
    TIMES_NEW_ROMAN("Times New Roman"),
    VERDANA("Verdana"),
    DIALOG("DIALOG"),
    RUNESCAPE("RuneScape");

    private final String name;

    public String toString() {
        return this.name;
    }

    SwarmerFonts(String name) {
        this.name = name;
    }

}
