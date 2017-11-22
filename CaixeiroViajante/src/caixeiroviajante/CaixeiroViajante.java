/*
 * Se for usar este código, cite o autor.
 */
package caixeiroviajante;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Random;
import org.moeaframework.problem.tsplib.TSPInstance;

/**
 *
 * @author Alexandre Romanelli <alexandre.romanelli@ifes.edu.br>
 */
public class CaixeiroViajante {

    private static int totalSolucoesAvaliadas = 0;
    private static int totalSolucoesBuscaLocal = 0;
    private static boolean imprimirAcompanhamento = false;

    /**
     * @param args os argumentos da linha de comando, que são os seguintes:<br/>
     *             <ol>
     *               <li>nome do arquivo da instância</li>
     *               <li>opção de meta-heurística (I: ILS; G: GRASP)</li>
     *               <li>opção de método construtivo (P: Vizinho mais próximo; A: Aleatório)</li>
     *               <li>opção de vizinhança (2: 2-Opt; I: Insertion; S: Swap)</li>
     *               <li>opção da busca local (P: primeiro aprimorante; M: melhor aprimorante)</li>
     *               <li>opção de acompanhamento pela interface (A: Imprimir acompanhamento; N: Não imprimir acompanhamento)</li>
     *               <li>número de iterações (no GRASP, é a quantidade de soluções iniciais; no ILS, é a quantidade de iterações sem melhoria até parar)</li>
     *               <li>taxa de aleatoriedade (usado no GRASP)</li>
     *             </ol>
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        TSPInstance instancia = new TSPInstance(new File(args[0]));
        int n = instancia.getDimension();
        double[][] distancia = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                distancia[i][j] = instancia.getDistanceTable().getDistanceBetween(i + 1, j + 1);
            }
        }
        
//        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
//        // linha com: NAME: ...
//        input.readLine();
//        // linha com: TYPE: ...
//        input.readLine();
//        // linha com: COMMENT: ...
//        input.readLine();
//        // linha com: DIMENSION: ...
//        String linha = input.readLine();
//        String[] item = linha.split(" ");
//        int n = Integer.parseInt(item[1]); // número com a dimensão da instância
//        // linha com: EDGE_WEIGHT_TYPE: ...
//        input.readLine();
//        // linha com: EDGE_WEIGHT_FORMAT: ...
//        input.readLine();
//        // linha com: EDGE_WEIGHT_SECTION
//        input.readLine();
//
//        int[][] distancia = new int[n][n];
//
//        // o código a seguir precisa ser corrigido para fazer a leitura com mais espaços entre os números
//        String numeros = "";
//        while (!(linha = input.readLine()).equals("EOF")) {
//            numeros += " " + linha.trim();
//        }
//        numeros = numeros.trim();
//
//        item = numeros.split(" ");
//        int k = 0;
//        for (int i = 0; i < n; i++) {
//            for (int j = 0; j <= i; j++) {
//                int dist = Integer.parseInt(item[k]);
//                k++;
//                distancia[i][j] = dist;
//                distancia[j][i] = dist;
//            }
//        }

        int[] solucao;
        // resolver o problema
        char opcaoMetaheuristica = 'I';
        if (args.length >= 2)
            opcaoMetaheuristica = args[1].charAt(0); // I=ILS;G=GRASP
        
        char opcaoConstrutivo = 'P';
        if (args.length >= 3)
            opcaoConstrutivo = args[2].charAt(0); // P="Vizinho mais próximo";A="Aleatório"
        
        char opcaoVizinhanca = '2';
        if (args.length >= 4)
            opcaoVizinhanca = args[3].charAt(0); // 2="2-Opt";I="Insertion";S="Swap-2"
        
        char opcaoAprimorante = 'P';
        if (args.length >= 5)
            opcaoAprimorante = args[4].charAt(0); // P="Primeiro aprimorante";M="Melhor aprimorante"
            
        char opcaoAcompanhamento = 'A';
        if (args.length >= 6)
            opcaoAcompanhamento = args[5].charAt(0); // A="Imprimir acompanhamento";N="Não imprimir acompanhamento"
            
        if (opcaoAcompanhamento == 'A')
                imprimirAcompanhamento = true;
        
        int iteracoes = 30;
        if (args.length >= 7)
            iteracoes = Integer.parseInt(args[6]);

        double taxaAleatoriedade = 0;
        if (args.length >= 8)
            taxaAleatoriedade = Integer.parseInt(args[7]) / 100.0;
        
        long tempoInicial = System.nanoTime();
        solucao = resolverCaixeiroViajante(n, distancia, opcaoConstrutivo, 
                    opcaoVizinhanca, opcaoAprimorante, opcaoMetaheuristica, 
                    iteracoes, taxaAleatoriedade);
        long tempoFinal = System.nanoTime();

        // imprimir resultado
        if (imprimirAcompanhamento)
            InterfaceTerminal.imprimirSolucao(TipoSolucao.MelhorSolucaoGlobal, n, solucao, distancia);
        else {
            System.out.println();
            InterfaceTerminal.imprimirSolucaoSimples(n, solucao, distancia);
        }
        DecimalFormat df = new DecimalFormat("###,###,###,##0");
        System.out.printf("\nQuantidade de soluções avaliadas: %15s\n", df.format(totalSolucoesAvaliadas));
        System.out.printf("\nTempo de execução...............: %15.3f s\n\n", (tempoFinal - tempoInicial) / 1000000000.0);
    }

    private static void obterListaDeVizinhosNaoVisitadosOrdenadaPorProximidade(int[] listaCandidatos, int origem, double[][] distancia, boolean[] visitado) {
        double[] distanciaOrigem = new double[listaCandidatos.length];
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
            double menorDistancia = distanciaOrigem[i];
            int indMenorDistancia = i;
            for (int j = i + 1; j < tamanhoLista; j++) {
                if (distanciaOrigem[j] < menorDistancia) {
                    menorDistancia = distanciaOrigem[j];
                    indMenorDistancia = j;
                }
            }
            double tempDistancia = distanciaOrigem[i];
            int tempCandidato = listaCandidatos[i];
            distanciaOrigem[i] = distanciaOrigem[indMenorDistancia];
            listaCandidatos[i] = listaCandidatos[indMenorDistancia];
            distanciaOrigem[indMenorDistancia] = tempDistancia;
            listaCandidatos[indMenorDistancia] = tempCandidato;
        }
    }

    public enum TipoSolucao {
        SolucaoInicial,
        SolucaoBaseVizinhanca,
        UltimaSolucaoVizinha,
        MelhorSolucaoVizinhanca,
        MelhorSolucaoLocal,
        MelhorSolucaoGlobal
    }
    

    private static int[] resolverCaixeiroViajante(int n, double[][] distancia,
            char construtivo, char opcaoVizinhanca, char opcaoAprimorante, 
            char opcaoMetaheuristica, int numIteracoes, double taxaAleatoriedade) {
        // prepara tela para acompanhamento
        String vizinhanca = "";
        switch (opcaoVizinhanca) {
            case '2':
                vizinhanca = "2-Opt";
                break;
            case 'I':
                vizinhanca = "Insertion";
                break;
            case 'S':
                vizinhanca = "Swap-2";
                break;
        }
        if (imprimirAcompanhamento) {
            InterfaceTerminal.prepararTelaAcompanhamento(construtivo, vizinhanca, 
                    opcaoAprimorante, opcaoMetaheuristica);
        }

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

        switch (opcaoMetaheuristica) {
            case 'G':
                executarGRASP(numIteracoes, taxaAleatoriedade, solucaoGlobal, 
                        n, solucao, solucaoBusca, solucaoMelhor, 
                        distancia, opcaoVizinhanca, opcaoAprimorante);
                break;
            case 'I':
                executarILS(solucaoGlobal, n, solucao, solucaoBusca, 
                        solucaoMelhor, distancia, construtivo, opcaoVizinhanca,
                        opcaoAprimorante, numIteracoes);
                break;
        }

        return solucaoGlobal;
    }

    // perturbacoes é um vetor que armazena as perturbações feitas desde a última melhoria
    private static int[][] perturbacoes;
    
    private static void executarILS(int[] solucaoILS,
            int n, int[] solucao, int[] solucaoBusca, int[] solucaoMelhor, 
            double[][] distancia, char construtivo, char opcaoVizinhanca, 
            char opcaoAprimorante, int numIteracoes) {
        switch (construtivo) {
            case 'P':
                gerarSolucaoGulosa(n, solucao, distancia, 
                        HeuristicaConstrutiva.VizinhoMaisProximo);
                totalSolucoesAvaliadas++;
                break;
            case 'A':
                gerarSolucaoAleatoria(n, solucao);
                totalSolucoesAvaliadas++;
                break;
        }
        if (imprimirAcompanhamento)
            InterfaceTerminal.imprimirSolucao(TipoSolucao.SolucaoInicial, n, 
                    solucao, distancia);
        executarBuscaLocal(n, solucao, solucaoBusca, solucaoILS, distancia, 
                opcaoVizinhanca, opcaoAprimorante);
        
        if (imprimirAcompanhamento) {
            InterfaceTerminal.imprimirSolucao(TipoSolucao.MelhorSolucaoLocal, n, 
                    solucaoILS, distancia);
            InterfaceTerminal.imprimirSolucao(TipoSolucao.MelhorSolucaoGlobal, n, 
                    solucaoILS, distancia);
        }
        
        perturbacoes = new int[numIteracoes][3];
        int iteracoesSemMelhoria = 0;
        int quantidadeIteracoes = 0;
        do {
            efetuarPerturbacao(n, solucao, solucaoILS, iteracoesSemMelhoria);
            
            if (imprimirAcompanhamento)
                InterfaceTerminal.imprimirSolucao(TipoSolucao.SolucaoInicial, n, 
                        solucao, distancia);

            executarBuscaLocal(n, solucao, solucaoBusca, solucaoMelhor, 
                    distancia, opcaoVizinhanca, opcaoAprimorante);
            
            if (imprimirAcompanhamento)
                InterfaceTerminal.imprimirSolucao(TipoSolucao.MelhorSolucaoLocal, n, 
                        solucaoMelhor, distancia);
            
            if (calcularCustoSolucao(n, solucaoMelhor, distancia) < 
                    calcularCustoSolucao(n, solucaoILS, distancia)) {
                System.arraycopy(solucaoMelhor, 0, solucaoILS, 0, n);
                
                if (imprimirAcompanhamento)
                    InterfaceTerminal.imprimirSolucao(TipoSolucao.MelhorSolucaoGlobal, n, 
                            solucaoILS, distancia);
                iteracoesSemMelhoria = 0;
            } else {
                iteracoesSemMelhoria++;
            }
            quantidadeIteracoes++;
            
            if (imprimirAcompanhamento) {
                InterfaceTerminal.imprimirQuantidadeIteracoesBuscaMetaheuristica(quantidadeIteracoes + 1);
                InterfaceTerminal.imprimirQuantidadeSolucoesGeradasMetaheuristica(totalSolucoesAvaliadas);
            }
        } while (iteracoesSemMelhoria < numIteracoes);
    }
    
    private static void efetuarPerturbacao(int n, int[] solucaoPerturbada,
            int[] solucaoBase, int indicePerturbacao) {
        Random r = new Random(Calendar.getInstance().getTimeInMillis());
        
        int pos1 = 0, pos2 = 0, pos3 = 0;
        boolean valido = true;
        do {
            pos1 = r.nextInt(n);
            
            while (pos2 == pos1) {
                pos2 = r.nextInt(n);
            }
            
            while (pos3 == pos1 || pos3 == pos2) {
                pos3 = r.nextInt(n);
            }
            
            valido = true;
            for (int i = 0; i < indicePerturbacao; i++) {
                if (pos1 == perturbacoes[i][0] &&
                        pos2 == perturbacoes[i][1] &&
                        pos3 == perturbacoes[i][2]) {
                    valido = false;
                    break;
                }
            }
            if (valido) {
                perturbacoes[indicePerturbacao][0] = pos1;
                perturbacoes[indicePerturbacao][1] = pos2;
                perturbacoes[indicePerturbacao][2] = pos3;

                System.arraycopy(solucaoBase, 0, solucaoPerturbada, 0, n);
                int temp = solucaoPerturbada[pos3];
                solucaoPerturbada[pos3] = solucaoPerturbada[pos2];
                solucaoPerturbada[pos2] = solucaoPerturbada[pos1];
                solucaoPerturbada[pos1] = temp;
            }
        } while (!valido);
    }
    
    private static void executarGRASP(int quantidadeIteracoes,
            double taxaAleatoriedade, int[] solucaoGRASP,
            int n, int[] solucao, int[] solucaoBusca, int[] solucaoMelhor, 
            double[][] distancia, char opcaoVizinhanca, char opcaoAprimorante) {
        
        double custoSolucaoGRASP = 0;
        
        for (int i = 0; i < quantidadeIteracoes; i++) {
            gerarSolucaoGulosaAleatoria(n, solucao, distancia, 
                    HeuristicaConstrutiva.VizinhoMaisProximo, taxaAleatoriedade);
            
            if (imprimirAcompanhamento)
                InterfaceTerminal.imprimirSolucao(TipoSolucao.SolucaoInicial, n,
                        solucao, distancia);
            
            executarBuscaLocal(n, solucao, solucaoBusca, solucaoMelhor, 
                    distancia, opcaoVizinhanca, opcaoAprimorante);
            if (i == 0) {
                System.arraycopy(solucaoMelhor, 0, solucaoGRASP, 0, n);
                custoSolucaoGRASP = calcularCustoSolucao(n, solucaoGRASP, distancia);
                
                if (imprimirAcompanhamento)
                    InterfaceTerminal.imprimirSolucao(TipoSolucao.MelhorSolucaoGlobal, n, 
                            solucaoGRASP, distancia);
            } else {
                if (calcularCustoSolucao(n, solucaoMelhor, distancia) < 
                        custoSolucaoGRASP) {
                    System.arraycopy(solucaoMelhor, 0, solucaoGRASP, 0, n);
                    custoSolucaoGRASP = calcularCustoSolucao(n, solucaoGRASP, distancia);
                    
                    if (imprimirAcompanhamento)
                        InterfaceTerminal.imprimirSolucao(TipoSolucao.MelhorSolucaoGlobal, n, 
                                solucaoGRASP, distancia);
                }
            }
            
            if (imprimirAcompanhamento) {
                InterfaceTerminal.imprimirQuantidadeIteracoesBuscaMetaheuristica(i + 1);
                InterfaceTerminal.imprimirQuantidadeSolucoesGeradasMetaheuristica(totalSolucoesAvaliadas);
            }
        }
    }

    private static void executarSimulatedAnnealing(int quantidadeIteracoes,
            int[] solucaoSimulatedAnnealing, int n, int[] solucao, 
            int[] solucaoBusca, int[] solucaoMelhor, double[][] distancia) {
        
    }

    private static void executarBuscaLocal(int n, int[] solucao,
            int[] solucaoBusca, int[] solucaoMelhor, double[][] distancia,
            char opcaoVizinhanca, char opcaoAprimorante) {
        totalSolucoesBuscaLocal = 0;
        boolean houveMelhoria;
        int iteracoes = 0;
        do {
            switch (opcaoVizinhanca) {
                case '2':
                    houveMelhoria = buscaEmVizinhanca2opt(n, solucao, solucaoBusca,
                            solucaoMelhor, distancia, opcaoAprimorante);
                    break;
                case 'I':
                    houveMelhoria = buscaEmVizinhancaInsert(n, solucao, solucaoBusca,
                            solucaoMelhor, distancia, opcaoAprimorante);
                    break;
                case 'S':
                    houveMelhoria = buscaEmVizinhancaSwap(n, solucao, solucaoBusca,
                            solucaoMelhor, distancia, opcaoAprimorante);
                    break;
                default:
                    houveMelhoria = false;
            }

            if (houveMelhoria) {
                if (imprimirAcompanhamento)
                    InterfaceTerminal.imprimirSolucao(TipoSolucao.MelhorSolucaoVizinhanca, n, solucaoMelhor, distancia);
                // copiar melhor soluçao vizinha para o vetor solucao (base para a proxima busca em vizinhança)
                System.arraycopy(solucaoMelhor, 0, solucao, 0, n);
            }
            iteracoes++;
            
            if (imprimirAcompanhamento) {
                InterfaceTerminal.imprimirQuantidadeIteracoesBuscaLocal(iteracoes);
                InterfaceTerminal.imprimirQuantidadeSolucoesGeradasBuscaLocal(totalSolucoesBuscaLocal);
            }
        } while (houveMelhoria);

        if (imprimirAcompanhamento)
            InterfaceTerminal.imprimirSolucao(TipoSolucao.MelhorSolucaoLocal, n, solucaoMelhor, distancia);
    }

    public static double calcularCustoSolucao(int n, int[] solucao, double[][] distancia) {
        double custo = 0;
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

    private static void gerarSolucaoAleatoria(int n, int[] solucao) {
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
            double[][] distancia, HeuristicaConstrutiva heuristica,
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
            double[][] distancia, HeuristicaConstrutiva heuristica) {
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
            int n, double[][] distancia, boolean[] visitado) {
        int indiceProximaCidade = -1;
        double menorDistancia = Double.MAX_VALUE;

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
            int[] solucaoVizinha, int[] solucaoMelhorVizinha, double[][] distancia,
            char opcaoAprimorante) {
        boolean houveMelhoria = false;
        int numVizinhos = 0;
        double custoMelhorSolucao = calcularCustoSolucao(n, solucaoBasica, distancia);
        
        if (imprimirAcompanhamento)
            InterfaceTerminal.imprimirSolucao(TipoSolucao.SolucaoBaseVizinhanca, n, solucaoBasica, distancia);

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
                if (imprimirAcompanhamento)
                    InterfaceTerminal.imprimirSolucao(TipoSolucao.UltimaSolucaoVizinha, n, solucaoVizinha, distancia);
    
                numVizinhos++;
                
                if (imprimirAcompanhamento)
                    InterfaceTerminal.imprimirQuantidadeSolucoesVizinhas(numVizinhos);

                double custoSolucaoVizinha = calcularCustoSolucao(n, solucaoVizinha, distancia);
                if (custoSolucaoVizinha < custoMelhorSolucao) {
                    System.arraycopy(solucaoVizinha, 0, solucaoMelhorVizinha, 0, n);
                    
                    if (imprimirAcompanhamento)
                        InterfaceTerminal.imprimirSolucao(TipoSolucao.MelhorSolucaoVizinhanca, n, solucaoMelhorVizinha, distancia);
    
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

    /**
     * A ser implementada. Este método faz a busca na vizinhança com estrutura
     * definida pela inserção de uma visita a uma cidade em outro ponto da rota.
     * @param n
     * @param solucaoBasica
     * @param solucaoVizinha
     * @param solucaoMelhorVizinha
     * @param distancia
     * @param opcaoAprimorante
     * @return 
     */
    private static boolean buscaEmVizinhancaInsert(int n, int[] solucaoBasica,
            int[] solucaoVizinha, int[] solucaoMelhorVizinha, double[][] distancia,
            char opcaoAprimorante) {
        boolean houveMelhoria = false;
        int numVizinhos = 0;
        double custoMelhorSolucao = calcularCustoSolucao(n, solucaoBasica, distancia);
        
        if (imprimirAcompanhamento)
            InterfaceTerminal.imprimirSolucao(TipoSolucao.SolucaoBaseVizinhanca, n, solucaoBasica, distancia);

        loopExterno:
        for (int i = 0; i < n; i++) {
            int cidadeI = solucaoBasica[i];
            for (int j = 0; j < n; j++) {
                System.arraycopy(solucaoBasica, 0, solucaoVizinha, 0, n);
                if (j == i || j == i - 1) // j == i - 1 é para evitar testar soluções idênticas a outras já testadas
                    continue;
                if (j < i) {
                    for (int k = i; k > j; k--)
                        solucaoVizinha[k] = solucaoVizinha[k - 1];
                } else {
                    for (int k = i; k < j; k++)
                        solucaoVizinha[k] = solucaoVizinha[k + 1];
                }
                solucaoVizinha[j] = cidadeI;
                // para teste: imprimir cada solução vizinha
                if (imprimirAcompanhamento)
                    InterfaceTerminal.imprimirSolucao(TipoSolucao.UltimaSolucaoVizinha, n, solucaoVizinha, distancia);
    
                numVizinhos++;
                
                if (imprimirAcompanhamento)
                    InterfaceTerminal.imprimirQuantidadeSolucoesVizinhas(numVizinhos);

                double custoSolucaoVizinha = calcularCustoSolucao(n, solucaoVizinha, distancia);
                if (custoSolucaoVizinha < custoMelhorSolucao) {
                    System.arraycopy(solucaoVizinha, 0, solucaoMelhorVizinha, 0, n);
                    
                    if (imprimirAcompanhamento)
                        InterfaceTerminal.imprimirSolucao(TipoSolucao.MelhorSolucaoVizinhanca, n, solucaoMelhorVizinha, distancia);
    
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

    /**
     * A ser implementada!
     * @param n
     * @param solucaoBasica
     * @param solucaoVizinha
     * @param solucaoMelhorVizinha
     * @param distancia
     * @param opcaoAprimorante
     * @return 
     */
    private static boolean buscaEmVizinhancaSwap(int n, int[] solucaoBasica,
            int[] solucaoVizinha, int[] solucaoMelhorVizinha, double[][] distancia,
            char opcaoAprimorante) {
        boolean houveMelhoria = false;
        int numVizinhos = 0;
        double custoMelhorSolucao = calcularCustoSolucao(n, solucaoBasica, distancia);
        
        if (imprimirAcompanhamento)
            InterfaceTerminal.imprimirSolucao(TipoSolucao.SolucaoBaseVizinhanca, n, solucaoBasica, distancia);

        loopExterno:
        for (int i = 0; i < n - 1; i++) {
            for (int j = i; j < n; j++) {
                System.arraycopy(solucaoBasica, 0, solucaoVizinha, 0, n);
                solucaoVizinha[i] = solucaoBasica[j];
                solucaoVizinha[j] = solucaoBasica[i];
                // para teste: imprimir cada solução vizinha
                if (imprimirAcompanhamento)
                    InterfaceTerminal.imprimirSolucao(TipoSolucao.UltimaSolucaoVizinha, n, solucaoVizinha, distancia);
    
                numVizinhos++;
                
                if (imprimirAcompanhamento)
                    InterfaceTerminal.imprimirQuantidadeSolucoesVizinhas(numVizinhos);

                double custoSolucaoVizinha = calcularCustoSolucao(n, solucaoVizinha, distancia);
                if (custoSolucaoVizinha < custoMelhorSolucao) {
                    System.arraycopy(solucaoVizinha, 0, solucaoMelhorVizinha, 0, n);
                    
                    if (imprimirAcompanhamento)
                        InterfaceTerminal.imprimirSolucao(TipoSolucao.MelhorSolucaoVizinhanca, n, solucaoMelhorVizinha, distancia);
    
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

    private static void geraSolucaoAleatoriaEmVizinhanca2opt(int n, 
            int[] solucaoBasica, int[] solucaoVizinha, double[][] distancia,
            Random geradorAleatorio) {
        
        if (imprimirAcompanhamento)
            InterfaceTerminal.imprimirSolucao(TipoSolucao.SolucaoBaseVizinhanca, n, solucaoBasica, distancia);

        int origemI = geradorAleatorio.nextInt(n);
        int destinoI = (origemI == n - 1) ? 0 : origemI + 1;
        int j = geradorAleatorio.nextInt(n - 3) + 1;
        int origemJ = (origemI + j + 1) % n;
        int destinoJ = (origemI + j + 2) % n;

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
        if (imprimirAcompanhamento)
            InterfaceTerminal.imprimirSolucao(TipoSolucao.UltimaSolucaoVizinha, n, solucaoVizinha, distancia);
    }

}
