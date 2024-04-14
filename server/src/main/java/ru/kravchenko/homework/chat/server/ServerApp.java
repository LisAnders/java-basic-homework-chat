package ru.kravchenko.homework.chat.server;

import java.util.Arrays;

public class ServerApp {
    public static void main(String[] args) {
        new Server(8189).start();
    }
}
