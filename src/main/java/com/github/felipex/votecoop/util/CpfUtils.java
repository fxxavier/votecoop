package com.github.felipex.votecoop.util;

import br.com.caelum.stella.validation.CPFValidator;
import br.com.caelum.stella.validation.InvalidStateException;

public class CpfUtils {

    private static final CPFValidator VALIDATOR = new CPFValidator();

    private CpfUtils() {}

    public static String gerarAleatorio() {
        return VALIDATOR.generateRandomValid();
    }

    public static boolean isValido(String cpf) {
        try {
            VALIDATOR.assertValid(cpf);
            return true;
        } catch (InvalidStateException e) {
            return false;
        }
    }
}
