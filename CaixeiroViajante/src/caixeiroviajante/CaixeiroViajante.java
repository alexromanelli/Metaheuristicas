/*
 * Se for usar este código, cite o autor.
 */
package caixeiroviajante;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Random;

/**
 *
 * @author Alexandre Romanelli <alexandre.romanelli@ifes.edu.br>
 */
public class CaixeiroViajante {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        // linha com: NAME: ...
        input.readLine();
        // linha com: TYPE: ...
        input.readLine();
        // linha com: COMMENT: ...
        input.readLine();
        // linha com: DIMENSION: ...
        String linha = input.readLine();
        String[] item = linha.split(" ");
        int n = Integer.parseInt(item[1]); // número com a dimensão da instância
        // linha com: EDGE_WEIGHT_TYPE: ...
        input.readLine();
        // linha com: EDGE_WEIGHT_FORMAT: ...
        input.readLine();
        // linha com: EDGE_WEIGHT_SECTION
        input.readLine();
        
        int[][] distancia = new int[n][n];
        
        // o código a seguir precisa ser corrigido para fazer a leitura com mais espaços entre os números
        
        String numeros = "";
        while (!(linha = input.readLine()).equals("EOF")) {
            numeros += " " + linha.trim();
        }
        numeros = numeros.trim();
        
        item = numeros.split(" ");
        int k = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j <= i; j++) {
                int dist = Integer.parseInt(item[k]);
                k++;
                distancia[i][j] = dist;
                distancia[j][i] = dist;
            }
        }
        
        // resolver o problema
        int[] solucao = resolverCaixeiroViajante(n, distancia);
        
        // imprimir resultado
        for (int i = 0; i < n; i++) {
            System.out.print((solucao[i] + 1) + (i < n - 1 ? ", " : ""));
        }
        System.out.println();
        System.out.println("Custo da solução: " + calcularCustoSolucao(n, solucao, distancia));
    }

    private static int[] resolverCaixeiroViajante(int n, int[][] distancia) {
        int[] solucao = new int[n];
        
        gerarSolucaoAleatoria(n, solucao, distancia);
        
        return solucao;
    }

    private static int calcularCustoSolucao(int n, int[] solucao, int[][] distancia) {
        int custo = 0;
        for (int i = 1; i < n; i++) {
            int indiceCidadeAnterior = solucao[i - 1];
            int indiceCidadeAtual = solucao[i];
            custo = custo + distancia[indiceCidadeAnterior][indiceCidadeAtual];
        }
        int indiceUltimaCidade = solucao[n - 1];
        int indicePrimeiraCidade = solucao[0];
        custo = custo + distancia[indiceUltimaCidade][indicePrimeiraCidade];

        return custo;
    }

    private static void gerarSolucaoAleatoria(int n, int[] solucao, int[][] distancia) {
        Random r = new Random(Calendar.getInstance().getTimeInMillis());
        boolean[] visitado = new boolean[n];
        for (int i = 0; i < n; i++) {
            int indiceCidade = r.nextInt(n);
            while (visitado[indiceCidade]) {
                indiceCidade = (indiceCidade + 1) % n;
            }
            solucao[i] = indiceCidade;
            visitado[indiceCidade] = true;
        }
    }
    
}
