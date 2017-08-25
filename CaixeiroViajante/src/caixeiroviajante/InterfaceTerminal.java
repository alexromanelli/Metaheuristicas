/*
 * Se for usar este código, cite o autor.
 */
package caixeiroviajante;

import java.text.DecimalFormat;

/**
 *
 * @author Alexandre Romanelli <alexandre.romanelli@ifes.edu.br>
 */
public class InterfaceTerminal {

    public static void imprimirQuantidadeSolucoesVizinhas(int quantidade) {
        gotoxy(21, 90);
        System.out.printf("%06d", quantidade);
    }

    public static void gotoxy(int row, int column) {
        char escCode = 27;
        System.out.print(String.format("%c[%d;%df", escCode, row, column));
    }

    public static void imprimirQuantidadeIteracoesBuscaLocal(int quantidade) {
        gotoxy(35, 126);
        System.out.printf("%05d", quantidade);
    }

    public static void prepararTelaAcompanhamento(char opcaoConstrutivo, String estruturaVizinhanca, char opcaoAprimorante, char opcaoMetaheuristica) {
        String metodoConstrutivo = "";
        switch (opcaoConstrutivo) {
            case 'A':
                metodoConstrutivo = "aleatorio";
                break;
            case 'G':
                metodoConstrutivo = "guloso-aleatorizado";
                break;
            case 'P':
                metodoConstrutivo = "heuristica vizinho mais proximo";
                break;
        }
        String opcaoDeAprimoramento = "";
        switch (opcaoAprimorante) {
            case 'P':
                opcaoDeAprimoramento = "primeiro aprimorante";
                break;
            case 'M':
                opcaoDeAprimoramento = "melhor aprimorante";
                break;
        }
        String metaheuristica = "";
        switch (opcaoMetaheuristica) {
            case 'G':
                metaheuristica = "GRASP";
                break;
            case 'I':
                metaheuristica = "ILS";
                break;
        }
        for (int i = 0; i < 100; i++) {
            System.out.println();
        }
        gotoxy(2, 3);
        System.out.print("Metaheur\u00edstica.........: " + metaheuristica);
        gotoxy(3, 3);
        System.out.print("Metodo construtivo.....: " + metodoConstrutivo);
        gotoxy(4, 3);
        System.out.print("Estrutura de vizinhan\u00e7a: " + estruturaVizinhanca);
        gotoxy(5, 3);
        System.out.print("Op\u00e7ao de aprimoramento.: " + opcaoDeAprimoramento);
        gotoxy(7, 3);
        System.out.print("Solu\u00e7ao inicial................:    |  custo = 00000000");
        gotoxy(14, 3);
        System.out.print("Solu\u00e7ao base da vizinhan\u00e7a.....:    |  custo = 00000000");
        gotoxy(21, 3);
        System.out.print("Ultima solu\u00e7ao vizinha gerada..:    |  custo = 00000000  |  solu\u00e7oes vizinhas geradas: 000000");
        gotoxy(28, 3);
        System.out.print("Melhor solu\u00e7ao desta vizinhan\u00e7a:    |  custo = 00000000");
        gotoxy(35, 3);
        System.out.print("Melhor solu\u00e7ao local...........:    |  custo = 00000000  |  total de solu\u00e7oes BL.....: 000000  | itera\u00e7oes da busca local: 00000");
        gotoxy(42, 3);
        System.out.print("Melhor solu\u00e7ao global..........:    |  custo = 00000000  |  total de solu\u00e7oes geradas: 000000  | itera\u00e7oes...............: 00000");
        //System.out.print(totalSolucoesAvaliadas);
        for (int i = 0; i < 6; i++) {
            System.out.println();
        }
    }
    
    public static void imprimirSolucaoSimples(int n, int[] solucao, int[][] distancia) {
        int custo = CaixeiroViajante.calcularCustoSolucao(n, solucao, distancia);
        DecimalFormat df = new DecimalFormat("00000000");
        System.out.println("Solução encontrada com custo " + df.format(custo) + ":");
        for (int i = 0; i < n; i++) {
            System.out.print((solucao[i] + 1) + (i < n - 1 ? ", " : ""));
        }
        System.out.println();
    }

    public static void imprimirSolucao(CaixeiroViajante.TipoSolucao tipo, int n, int[] solucao, int[][] distancia) {
        int linha = 0;
        int coluna = 0;
        switch (tipo) {
            case SolucaoInicial:
                linha = 9;
                break;
            case SolucaoBaseVizinhanca:
                linha = 16;
                break;
            case UltimaSolucaoVizinha:
                linha = 23;
                break;
            case MelhorSolucaoVizinhanca:
                linha = 30;
                break;
            case MelhorSolucaoLocal:
                linha = 37;
                break;
            case MelhorSolucaoGlobal:
                linha = 44;
                break;
        }
        gotoxy(linha, coluna);
        for (int i = 0; i < n; i++) {
            System.out.print((solucao[i] + 1) + (i < n - 1 ? ", " : ""));
        }
        gotoxy(linha - 2, 50);
        int custo = CaixeiroViajante.calcularCustoSolucao(n, solucao, distancia);
        DecimalFormat df = new DecimalFormat("00000000");
        System.out.print(df.format(custo));
        gotoxy(51, 0);
    }

    public static void imprimirQuantidadeSolucoesGeradasBuscaLocal(int quantidade) {
        gotoxy(35, 90);
        System.out.printf("%06d", quantidade);
    }

    public static void imprimirQuantidadeIteracoesBuscaMetaheuristica(int quantidade) {
        gotoxy(42, 126);
        System.out.printf("%05d", quantidade);
    }

    public static void imprimirQuantidadeSolucoesGeradasMetaheuristica(int quantidade) {
        gotoxy(42, 90);
        System.out.printf("%06d", quantidade);
    }
    
}
