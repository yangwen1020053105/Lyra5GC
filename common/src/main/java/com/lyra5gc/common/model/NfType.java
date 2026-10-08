package com.lyra5gc.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * NFType — TS 29.510 Table 6.1.6.3.3-1.
 */
public enum NfType {
    NRF, UDM, AMF, SMF, AUSF, NEF, PCF, SMSF, NSSF, UDR, LMF, GMLC,
    EIR_5G("5G_EIR"), SEPP, UPF, N3IWF, AF, UDSF, BSF, CHF, NWDAF, PCSCF,
    CBCF, HSS, UCMF, SOR_AF, SPAF, MME, SCSAS, SCEF, SCP, DCCF, MB_SMF,
    TSCTSF, ADRF, GBA_BSF, CEF, MB_UPF, NSWOF, PKMF, MNPF, SMS_GMSC,
    SMS_IWMSC, MBSF, MBSTF, PANF, IP_SM_GW, SMS_ROUTER, DCSMF, MRF, MRF_MA;

    private final String value;

    NfType() {
        this.value = name();
    }

    NfType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static NfType fromString(String s) {
        if (s == null) {
            return null;
        }
        for (NfType t : values()) {
            if (t.value.equalsIgnoreCase(s)) {
                return t;
            }
        }
        return null;
    }
}
