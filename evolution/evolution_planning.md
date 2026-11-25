# 12.1 Planejando as refatorações

## Guilherme Noronha

# 1\. Objetivo da tarefa

Determinar  o que será refatorado e por quê, antes de alterar o código.

# 2\. Descrição

A seção 10 terminou com a recomendação de refatorar pontualmente os componentes de maior risco, em vez de corrigir violações de formatação em massa. O roteiro exige pelo menos 5 *refactorings* diferentes, cerca de 100 linhas de diferença e uma motivação baseada em sintomas das ferramentas, então a escolha precisa ser feita com critério e registrada. Também é aqui que se guardam as métricas iniciais (rodando o CK antes de qualquer mudança), porque sem elas a comparação da tarefa 12.3 não teria valor. O plano orienta todas as tarefas de Evolução seguintes.

# 3\. Resultados/Planejamento

## 3.1 (R1)  ORUR01Handler.parseObs()

O método parseObs(), da classe ORUR01Handler, converte um segmento OBX de uma mensagem HL7 em um objeto Obs. É um método longo, com cerca de 239 linhas na versão analisada, e foi apontado pelo CK com wmc 56 e loc 221\. A leitura qualitativa feita na seção 10 identificou condicionais extensas e aninhadas e várias responsabilidades no mesmo corpo, como validação dos valores, obtenção do conceito, inicialização da observação, tratamento de exceções e conversão de valores por datatype.   
De um modo geral, isso indica baixa testabilidade e dificuldade de manutenção. A estrutura é uma cadeia de if/else if por *datatype* HL7 e o ramo NM concentra o aninhamento mais profundo. O planejamento aplica dois refactorings do catálogo de Fowler: *Extract Method*, extraindo três a quatro trechos coesos (por exemplo, a conversão do valor por *datatype* e a inicialização da observação), e *Replace Nested Conditional with Guard Clauses*, substituindo o aninhamento por retornos antecipados.   
A ferramenta que detectou o alvo é o CK, e a reavaliação deve mostrar queda de wmc, loc e aninhamento do método. Na classe, o wmc total deve variar pouco, porque *Extract Method* redistribui a complexidade, enquanto totalMethodsQty e rfc tendem a subir. A estimativa é de 60 a 90 linhas tocadas para *Extract Method* e 15 a 25 para os *guard clauses*.

**Localização exata:** Arquivo ORUR01Handler.java, linha 594\.

## 3.2 (R2)  ObsValidator.validateHelper()

	O método validateHelper(), da classe ObsValidator, valida uma observação e, recursivamente, os seus filhos. Ele é o método de maior complexidade entre os analisados, com wmc 60, medido pelo CK. O problema é a concentração de muitas validações condicionais por tipo de valor em um único método, o que indica regras de validação acopladas e difíceis de testar isoladamente. Na leitura do código, o bloco por *datatype* repete, em quase todos os ramos, o mesmo padrão de tratamento de erro conforme se trate ou não do nó raiz, o que caracteriza duplicação local.   
O planejamento aplica *Decompose Conditional* e *Extract Method*, caracterizado por um método por grupo de validação (por exemplo, por datatype do conceito), um método orquestrador curto e um método auxiliar para o padrão de erro repetido. A ferramenta que detectou o alvo é o CK. Espera-se forte queda do wmc do método, com estimativa de 40 a 60 linhas tocadas.

**Localização exata:** Arquivo ObsValidator.java, linha 105\.

## 3.3 (R3)  NaturalStrings.compareNatural()

	O método compareNatural() da classe NaturalStrings implementa a comparação “natural” de cadeias, em que blocos numéricos são comparados pelo valor. Foi apontado pelo CK com wmc 49 e loc 146\. O problema é um algoritmo monolítico, em que laços e comparações de blocos numéricos e não numéricos estão misturados em um único método. A refatoração prevista é *Extract Method*, separando a leitura de blocos numéricos da comparação de blocos.   
É o alvo de maior risco do plano, porque a lógica é sutil e as duas partes compartilham índices e variáveis de laço, de modo que a extração pode exigir passar ou devolver esse estado. Por isso, a mudança só deve ser feita se a suíte existente cobrir o método. A detecção foi feita pelo CK, e a reavaliação deve mostrar queda em wmc, loc, loopQty e comparisonsQty do método. A estimativa é de 40 a 60 linhas tocadas.

**Localização exata:** Arquivo NaturalStrings.java, linha 202\.

## 3.4 (R4) *double-checked locking* em Context.java (mailSession)

	O PMD identificou, em Context.java (método getMailSession()), o padrão *double-checked locking* sobre o atributo estático mailSession, declarado sem *volatile*. Sem a garantia de visibilidade entre *threads*, esse padrão pode expor estado desatualizado, o que é um risco potencial de concorrência, descrito na triagem visual da seção 10\. O planejamento propõe declarar o campo como *volatile* ou adotar o *holder idiom*.   
Em termos de Fowler, a mudança só se aproxima de *Substitute Algorithm*, pois o catálogo não tem um *refactoring* exato para esse caso. A ferramenta que detectou o alvo é o PMD (regra *DoubleCheckedLocking*, uma ocorrência), e a verificação consiste em re-executar a ferramenta e constatar o desaparecimento do alerta. O efeito sobre as métricas do CK deve ser praticamente nulo. A estimativa é de 3 a 6 linhas tocadas.