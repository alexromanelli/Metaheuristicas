/*
 * Se for usar este código, cite o autor.
 */
package caixeiroviajante;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Random;

/**
 *
 * @author Alexandre Romanelli <alexandre.romanelli@ifes.edu.br>
 */
public class CaixeiroViajante {

    private static int totalSolucoesAvaliadas = 0;
    private static int totalSolucoesBuscaLocal = 0;

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

        int[] solucao;
        // resolver o problema
        if (args.length >= 2) {
            char opcaoConstrutivo = args[0].charAt(0);
            char opcaoAprimorante = args[1].charAt(0);
            solucao = resolverCaixeiroViajante(n, distancia, opcaoConstrutivo, opcaoAprimorante);
        } else {
            solucao = resolverCaixeiroViajante(n, distancia, 'P', 'M');
        }

        // imprimir resultado
        imprimirSolucao(TipoSolucao.MelhorSolucaoGlobal, n, solucao, distancia);

//        System.out.println("\nTotal de soluçoes avaliadas: " + totalSolucoesAvaliadas + "\n");
    }

    private static void obterListaDeVizinhosNaoVisitadosOrdenadaPorProximidade(int[] listaCandidatos, int origem, int[][] distancia, boolean[] visitado) {
        int[] distanciaOrigem = new int[listaCandidatos.length];
        int tamanhoLista = 0;
        for (int i = 0; i < listaCandidatos.length; i++) {
            if (i != origem && !visitado[i]) {
                listaCandidatos[tamanhoLista] = i;
                distanciaOrigem[tamanhoLista] = distancia[origem][i];
                tamanhoLista++;
            }
        }
        
        // ordenar lista por distâncias crescentes (ordenação por seleção)
        for (int i = 0; i < tamanhoLista - 1; i++) {
            int menorDistancia = distanciaOrigem[i];
            int indMenorDistancia = i;
            for (int j = i + 1; j < tamanhoLista; j++) {
                if (distanciaOrigem[j] < menorDistancia) {
                    menorDistancia = distanciaOrigem[j];
                    indMenorDistancia = j;
                }
            }
            int tempDistancia = distanciaOrigem[i];
            int tempCandidato = listaCandidatos[i];
            distanciaOrigem[i] = distanciaOrigem[indMenorDistancia];
            listaCandidatos[i] = listaCandidatos[indMenorDistancia];
            distanciaOrigem[indMenorDistancia] = tempDistancia;
            listaCandidatos[indMenorDistancia] = tempCandidato;
        }
    }

    private enum TipoSolucao {
        SolucaoInicial,
        SolucaoBaseVizinhanca,
        UltimaSolucaoVizinha,
        MelhorSolucaoVizinhanca,
        MelhorSolucaoLocal,
        MelhorSolucaoGlobal
    }
    
    private static void imprimirSolucao(TipoSolucao tipo, int n, int[] solucao, int[][] distancia) {
        int linha = 0, coluna = 0;
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
        int custo = calcularCustoSolucao(n, solucao, distancia);
        DecimalFormat df = new DecimalFormat("00000000");
        System.out.print(df.format(custo));
        gotoxy(51, 0);
    }
    
    private static void imprimirQuantidadeSolucoesVizinhas(int quantidade) {
        gotoxy(21, 90);
        System.out.printf("%06d", quantidade);
    }
    
    private static void imprimirQuantidadeSolucoesGeradasBuscaLocal(int quantidade) {
        gotoxy(35, 90);
        System.out.printf("%06d", quantidade);
    }
    
    private static void imprimirQuantidadeIteracoesBuscaLocal(int quantidade) {
        gotoxy(35, 126);
        System.out.printf("%05d", quantidade);
    }
    
    private static void imprimirQuantidadeSolucoesGeradasMetaheuristica(int quantidade) {
        gotoxy(42, 90);
        System.out.printf("%06d", quantidade);
    }
    
    private static void imprimirQuantidadeIteracoesBuscaMetaheuristica(int quantidade) {
        gotoxy(42, 126);
        System.out.printf("%05d", quantidade);
    }
    
    private static void prepararTelaAcompanhamento(char opcaoConstrutivo, 
            String estruturaVizinhanca, char opcaoAprimorante) {
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
        
        for (int i = 0; i < 100; i++)
            System.out.println();
        gotoxy(3, 3);
        System.out.print("Metodo construtivo.....: " + metodoConstrutivo);
        gotoxy(4, 3);
        System.out.print("Estrutura de vizinhança: " + estruturaVizinhanca);
        gotoxy(5, 3);
        System.out.print("Opçao de aprimoramento.: " + opcaoDeAprimoramento);
        gotoxy(7, 3);
        System.out.print("Soluçao inicial................:    |  custo = 00000000");
        gotoxy(14, 3);
        System.out.print("Soluçao base da vizinhança.....:    |  custo = 00000000");
        gotoxy(21, 3);
        System.out.print("Ultima soluçao vizinha gerada..:    |  custo = 00000000  |  soluçoes vizinhas geradas: 000000");
        gotoxy(28, 3);
        System.out.print("Melhor soluçao desta vizinhança:    |  custo = 00000000");
        gotoxy(35, 3);
        System.out.print("Melhor soluçao local...........:    |  custo = 00000000  |  total de soluçoes BL.....: 000000  | iteraçoes da busca local: 00000");
        gotoxy(42, 3);
        System.out.print("Melhor soluçao global..........:    |  custo = 00000000  |  total de soluçoes geradas: 000000  | iteraçoes...............: 00000");
        //System.out.print(totalSolucoesAvaliadas);
        
        for (int i = 0; i < 7; i++)
            System.out.println();
    }

    private static int[] resolverCaixeiroViajante(int n, int[][] distancia,
            char construtivo, char opcaoAprimorante) {
        // prepara tela para acompanhamento
        prepararTelaAcompanhamento(construtivo, "2-Opt", opcaoAprimorante);

        int[] solucao = new int[n];
        int[] solucaoBusca = new int[n];
        int[] solucaoMelhor = new int[n];
        int[] solucaoGlobal = new int[n];

//        switch (construtivo) {
//            case 'A':
//                gerarSolucaoAleatoria(n, solucao, distancia);
//                break;
//            case 'G':
//                gerarSolucaoGulosaAleatoria(n, solucao, distancia, HeuristicaConstrutiva.VizinhoMaisProximo, 0.1);
//                break;
//            case 'P':
//                gerarSolucaoGulosa(n, solucao, distancia, HeuristicaConstrutiva.VizinhoMaisProximo);
//                break;
//        }
//        totalSolucoesAvaliadas++;
//        imprimirSolucao(TipoSolucao.SolucaoInicial, n, solucao, distancia);
//
//        System.arraycopy(solucao, 0, solucaoMelhor, 0, n);
//
//        executarBuscaLocalHillClimbing(n, solucao, solucaoBusca, solucaoMelhor,
//                distancia, opcaoAprimorante);

        executarGRASP(30, solucaoGlobal, n, solucao, solucaoBusca, solucaoMelhor, 
                distancia, opcaoAprimorante);

        return solucaoGlobal;
    }
    
    private static void executarGRASP(int quantidadeIteracoes,
            int[] solucaoGRASP,
            int n, int[] solucao, int[] solucaoBusca, int[] solucaoMelhor, 
            int[][] distancia, char opcaoAprimorante) {
        
        int custoSolucaoGRASP = 0;
        
        for (int i = 0; i < quantidadeIteracoes; i++) {
            gerarSolucaoGulosaAleatoria(n, solucao, distancia, 
                    HeuristicaConstrutiva.VizinhoMaisProximo, 0.1);
            imprimirSolucao(TipoSolucao.SolucaoInicial, n,
                    solucao, distancia);
            
            executarBuscaLocalHillClimbing(n, solucao, solucaoBusca, 
                    solucaoMelhor, distancia, opcaoAprimorante);
            if (i == 0) {
                System.arraycopy(solucaoMelhor, 0, solucaoGRASP, 0, n);
                custoSolucaoGRASP = calcularCustoSolucao(n, solucaoGRASP, distancia);
                imprimirSolucao(TipoSolucao.MelhorSolucaoGlobal, n, 
                        solucaoGRASP, distancia);
            } else {
                if (calcularCustoSolucao(n, solucaoMelhor, distancia) < 
                        custoSolucaoGRASP) {
                    System.arraycopy(solucaoMelhor, 0, solucaoGRASP, 0, n);
                    custoSolucaoGRASP = calcularCustoSolucao(n, solucaoGRASP, distancia);
                    imprimirSolucao(TipoSolucao.MelhorSolucaoGlobal, n, 
                            solucaoGRASP, distancia);
                }
            }
            imprimirQuantidadeIteracoesBuscaMetaheuristica(i + 1);
            imprimirQuantidadeSolucoesGeradasMetaheuristica(totalSolucoesAvaliadas);
        }
    }

    private static void executarBuscaLocalHillClimbing(int n, int[] solucao,
            int[] solucaoBusca, int[] solucaoMelhor, int[][] distancia,
            char opcaoAprimorante) {
        totalSolucoesBuscaLocal = 0;
        boolean houveMelhoria;
        int iteracoes = 0;
        do {
            houveMelhoria = buscaEmVizinhanca2opt(n, solucao, solucaoBusca,
                    solucaoMelhor, distancia, opcaoAprimorante);

            if (houveMelhoria) {
                imprimirSolucao(TipoSolucao.MelhorSolucaoVizinhanca, n, solucaoMelhor, distancia);
                // copiar melhor soluçao vizinha para o vetor solucao (base para a proxima busca em vizinhança)
                System.arraycopy(solucaoMelhor, 0, solucao, 0, n);
            }
            iteracoes++;
            imprimirQuantidadeIteracoesBuscaLocal(iteracoes);
            imprimirQuantidadeSolucoesGeradasBuscaLocal(totalSolucoesBuscaLocal);
        } while (houveMelhoria);

        imprimirSolucao(TipoSolucao.MelhorSolucaoLocal, n, solucaoMelhor, distancia);

//        System.out.println("\nTotal de iteraçoes da busca local: " + iteracoes + "\n");
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

    enum HeuristicaConstrutiva {
        VizinhoMaisProximo,
        InsercaoMaisBarata
    }
    
    private static void gerarSolucaoGulosaAleatoria(int n, int[] solucao,
            int[][] distancia, HeuristicaConstrutiva heuristica,
            double taxaDeAleatoriedade) {
        // implementar o método como sugerido no GRASP
        Random r = new Random(Calendar.getInstance().getTimeInMillis());
        boolean[] visitado = new boolean[n];
        int[] listaCandidatos = new int[n];

        // definir ponto de partida arbitrariamente como o primeiro local do vetor
        solucao[0] = 0;
        visitado[0] = true;
        
        // completar a solução
        for (int i = 1; i < n; i++) {
            // gerar lista de candidatos
            int quantidadeCandidatos = n - i;
            switch (heuristica) {
                case VizinhoMaisProximo:
                    obterListaDeVizinhosNaoVisitadosOrdenadaPorProximidade(listaCandidatos, solucao[i - 1], distancia, visitado);
                    break;
                case InsercaoMaisBarata:
                    break;
            }
            
            // fazer lista restrita de candidatos
            int quantidadeRestritaCandidatos = (int) Math.ceil(quantidadeCandidatos * taxaDeAleatoriedade);
            
            // selecionar aleatoriamente da lista restrita de candidatos
            int indSelecionado = r.nextInt(quantidadeRestritaCandidatos == 0 ? 1 : quantidadeRestritaCandidatos);
            
            // incluir selecionado na solução
            solucao[i] = listaCandidatos[indSelecionado];
            visitado[listaCandidatos[indSelecionado]] = true;
        }
    }

    private static void gerarSolucaoGulosa(int n, int[] solucao,
            int[][] distancia, HeuristicaConstrutiva heuristica) {
        boolean[] visitado = new boolean[n];
        int indiceUltimaCidadeVisitada = -1;
        for (int i = 0; i < n; i++) {
            int indiceCidadeEscolhida = -1;
            switch (heuristica) {
                case VizinhoMaisProximo:
                    if (i == 0) {
                        indiceCidadeEscolhida = 0;
                    } else {
                        indiceCidadeEscolhida
                                = obterCidadeMaisProximaNaoVisitada(indiceUltimaCidadeVisitada,
                                        n, distancia, visitado);
                    }
                    break;
                case InsercaoMaisBarata:
                    break;
            }
            solucao[i] = indiceCidadeEscolhida;
            visitado[indiceCidadeEscolhida] = true;
            indiceUltimaCidadeVisitada = indiceCidadeEscolhida;
        }
    }

    private static int obterCidadeMaisProximaNaoVisitada(int indiceCidadeOrigem,
            int n, int[][] distancia, boolean[] visitado) {
        int indiceProximaCidade = -1;
        int menorDistancia = Integer.MAX_VALUE;

        for (int i = 0; i < n; i++) {
            if (!visitado[i]) {
                if (distancia[indiceCidadeOrigem][i] < menorDistancia) {
                    indiceProximaCidade = i;
                    menorDistancia = distancia[indiceCidadeOrigem][i];
                }
            }
        }

        return indiceProximaCidade;
    }

    private static boolean buscaEmVizinhanca2opt(int n, int[] solucaoBasica,
            int[] solucaoVizinha, int[] solucaoMelhorVizinha, int[][] distancia,
            char opcaoAprimorante) {
        boolean houveMelhoria = false;
        int numVizinhos = 0;
        int custoMelhorSolucao = calcularCustoSolucao(n, solucaoBasica, distancia);
        
        imprimirSolucao(TipoSolucao.SolucaoBaseVizinhanca, n, solucaoBasica, distancia);

        loopExterno:
        for (int i = 0; i < n; i++) {
            int origemI = i;
            int destinoI = (i == n - 1) ? 0 : i + 1;
            for (int j = 1; j < n - 2; j++) {
                int origemJ = (i + j + 1) % n;
                int destinoJ = (i + j + 2) % n;

                int k = 0;
                solucaoVizinha[k] = solucaoBasica[origemI];
                k++;
                solucaoVizinha[k] = solucaoBasica[origemJ];
                k++;
                if (origemJ > destinoI) {
                    for (int l = origemJ - 1; l >= destinoI; l--) {
                        solucaoVizinha[k] = solucaoBasica[l];
                        k++;
                    }
                } else {
                    for (int l = origemJ - 1; l >= 0; l--) {
                        solucaoVizinha[k] = solucaoBasica[l];
                        k++;
                    }
                    for (int l = n - 1; l >= destinoI; l--) {
                        solucaoVizinha[k] = solucaoBasica[l];
                        k++;
                    }
                }
                solucaoVizinha[k] = solucaoBasica[destinoJ];
                k++;
                int quantCidadesEntreDestinoJeOrigemI = n - k;
                for (int l = 1; l <= quantCidadesEntreDestinoJeOrigemI; l++) {
                    solucaoVizinha[k] = solucaoBasica[(destinoJ + l) % n];
                    k++;
                }
                // para teste: imprimir cada solução vizinha
                imprimirSolucao(TipoSolucao.UltimaSolucaoVizinha, n, solucaoVizinha, distancia);
                numVizinhos++;
                imprimirQuantidadeSolucoesVizinhas(numVizinhos);

                int custoSolucaoVizinha = calcularCustoSolucao(n, solucaoVizinha, distancia);
                if (custoSolucaoVizinha < custoMelhorSolucao) {
                    System.arraycopy(solucaoVizinha, 0, solucaoMelhorVizinha, 0, n);
                    imprimirSolucao(TipoSolucao.MelhorSolucaoVizinhanca, n, solucaoMelhorVizinha, distancia);
                    custoMelhorSolucao = custoSolucaoVizinha;
                    houveMelhoria = true;

                    if (opcaoAprimorante == 'P') {
                        // se encontrou vizinho melhor, terminou a busca nesta vizinhança
                        break loopExterno;
                    }
                }
            }
        }
        //System.out.println("\nNúmero de vizinhos: " + numVizinhos);
        totalSolucoesAvaliadas += numVizinhos;
        totalSolucoesBuscaLocal += numVizinhos;

        return houveMelhoria;
    }

    private static void gotoxy(int row, int column) {
        char escCode = 0x1B;
        System.out.print(String.format("%c[%d;%df", escCode, row, column));
    }
}
