package org.design_system;

import org.design_system.Persistense.FilePersistanse;
import org.design_system.Persistense.NIOFilePersistanse;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {

        FilePersistanse persistanse = new NIOFilePersistanse("user.csv");

        persistanse.write("Maria Cabrera, Av Espana 409, 099789789");

        System.out.println(persistanse.findAll());


    }
}
