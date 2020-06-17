package com.company;

import java.net.*;
import java.util.*;
import java.io.*;

public class Crawler {
    public static final String URL_PREFIX = "<a href=\"http";
    static LinkedList<URLDepthPair> findLink = new LinkedList<>();
    static LinkedList<URLDepthPair> resultLink = new LinkedList<>();

    public static void showResult(LinkedList<URLDepthPair> resultLink) {
        for (URLDepthPair c : resultLink)
            System.out.println("Depth : " + c.getDepth() + "\tLink : " + c.toString());
    }

    public static boolean check(LinkedList<URLDepthPair> resultLink, URLDepthPair pair) {
        boolean isAlready = true;
        for (URLDepthPair c : resultLink)
            if (c.toString().equals(pair.toString()))
                isAlready = false;
        return isAlready;
    }

    public static void request(PrintWriter out, URLDepthPair pair) {
        out.println("GET " + pair.getPath() + " HTTP/1.1");
        out.println("Host: " + pair.getHost());
        out.println("Connection: close");
        out.println();
        out.flush();
    }

    public static void searchURLs(String urlString, int maxDepth) {
        URLDepthPair urlPair = new URLDepthPair(urlString, 0);
        try {
            findLink.add(urlPair);
            while (!findLink.isEmpty()) {
                URLDepthPair currentPair = findLink.removeFirst();
                int depth = currentPair.getDepth();
                try {
                    Socket s = new Socket(currentPair.getHost(), 80);
                    s.setSoTimeout(1000);
                    PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    request(out, currentPair);
                    String line;
                    while ((line = in.readLine()) != null) {
                        if (line.indexOf(URL_PREFIX) > 0 && depth < maxDepth) {
                            boolean isLinkFound = false;
                            StringBuilder currentLink = new StringBuilder();
                            char c = line.charAt(line.indexOf(URL_PREFIX) + 9);
                            currentLink.append(c);
                            for (int i = line.indexOf(URL_PREFIX) + 10; c != '"' && i < line.length() - 1; i++) {
                                c = line.charAt(i);
                                if (c == '"') isLinkFound = true;
                                else currentLink.append(c);
                            }
                            if (isLinkFound) {
                                URLDepthPair newPair = new URLDepthPair(currentLink.toString(), depth + 1);
                                if (check(findLink, newPair)) {
                                    findLink.add(newPair);
                                }
                            }
                        }
                    }
                    s.close();

                    if (check(resultLink, currentPair)) resultLink.add(currentPair);
                } catch (IOException e) {
                }
            }
            showResult(resultLink);
        } catch (NullPointerException e) {
            System.out.println("Not Link");
        }
    }

    public static void main(String[] args) {
        args = new String[]{"http://government.ru/", "1"};
        if (args.length != 2) {
            System.out.println("usage: java Crawler <URL><depth>");
            System.exit(1);
        } else {
            try {
                // второй аргумент долржен быть целым числом, выполняем конвертацию
                Integer.parseInt(args[1]);
            } catch (NumberFormatException nfe) {
                // если конвертация в int не удалась, печатаем ошибку
                System.out.println("usage: java Crawler <URL> <depth>");
                System.exit(1);
            }
        }
        searchURLs(args[0], Integer.parseInt(args[1]));
    }
    private static LinkedList<String> getAllLinks(URLDepthPair myDepthPair) {
        LinkedList<String> URLs = new LinkedList<>();
        Socket sock;
        // пытаемся создать новый сокет
        try {
            sock = new Socket(myDepthPair.getHost(), 80);
        }
        catch (UnknownHostException e) {
            System.err.println("UnknownHostException: " + e.getMessage());
            return URLs;
        }
        catch (IOException ex) {
            System.err.println("IOException: " + ex.getMessage());
            return URLs;
        }
        try {
            sock.setSoTimeout(3000);
        }
        catch (SocketException exc) {
            System.err.println("SocketException: " + exc.getMessage());
            return URLs;
        }
        String docPath = myDepthPair.getPath();
        String webHost = myDepthPair.getHost();
        OutputStream outStream;

        // пытаемся получить выходной поток от сокета
        try {
            outStream = sock.getOutputStream();
        }
        catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            return URLs;
        }


        PrintWriter myWriter = new PrintWriter(outStream, true);


        myWriter.println("GET " + docPath + " HTTP/1.1");
        myWriter.println("Host: " + webHost);
        myWriter.println("Connection: close");
        myWriter.println();


        InputStream inStream;


        try {
            inStream = sock.getInputStream();
        }

        catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            return URLs;
        }

        InputStreamReader inStreamReader = new InputStreamReader(inStream);
        BufferedReader BuffReader = new BufferedReader(inStreamReader);


        while (true) {
            String line;
            try {
                line = BuffReader.readLine();
            }

            catch (IOException except) {
                System.err.println("IOException: " + except.getMessage());
                return URLs;
            }

            if (line == null)
                break;


            int beginIndex = 0;
            int endIndex = 0;
            int index = 0;

            while (true) {
                String URL_INDICATOR = "a href=\"";
                String END_URL = "\"";


                index = line.indexOf(URL_INDICATOR, index);
                if (index == -1) // No more copies of start in this line
                    break;


                index += URL_INDICATOR.length();
                beginIndex = index;


                endIndex = line.indexOf(END_URL, index);
                index = endIndex;


                String newLink = line.substring(beginIndex, endIndex);
                URLs.add(newLink);
            }
        }
        return URLs;
    }
}
