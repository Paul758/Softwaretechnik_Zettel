package org.mhisoft.wallet.view;

import org.mhisoft.wallet.model.ItemType;
import org.mhisoft.wallet.model.WalletItem;
import org.mhisoft.wallet.model.WalletModel;
import org.mhisoft.wallet.service.ServiceRegistry;

import java.util.*;

public class Filter {


    // filterTerm = "Photoshop AND Steam OR EPIC GAMES";

    //String filterTerm = "Photoshop AND Steam OR Sparkasse";

    String filterTerm = "Steam OR Spar AND P OR Com AND E AND B AND E OR Spar AND P OR Photo OR Commerz OR E OR P";

    String filterTermTest = "Steam OR (Spar AND P) OR ((Com AND E) AND B)) AND Epic) OR Spar AND P OR Photo";

    String filterTerm2 = "Photo OR S AND T ";

    String filterTerm3 = "H OR K AND S OR C";

    WalletModel walletModel = ServiceRegistry.instance.getWalletModel();
    ArrayList<WalletItem> resultsList = new ArrayList<>();

    ArrayList<String> expressions = new ArrayList<>();

    HashMap<Integer, String> atomicTerms = new HashMap<>();
    HashMap<Integer, String> booleanOperators = new HashMap<>();

    HashMap<Integer, HashSet<String>> firstEvaluatedExpressions = new HashMap<>();

    HashMap<Integer,HashSet<String>> secondEvaluatedExpressions = new HashMap<>();

    public Filter(){
        WalletItem walletItem1 = new WalletItem(ItemType.item, "Photoshop");
        WalletItem walletItem2 = new WalletItem(ItemType.item, "Steam");
        //WalletItem walletItem3 = new WalletItem(ItemType.item, "Epic Games");
        WalletItem walletItem3 = new WalletItem(ItemType.item, "Sparkasse");
        WalletItem walletItem4 = new WalletItem(ItemType.item, "Commerzbank");
        WalletItem walletItem5 = new WalletItem(ItemType.item, "Epic Games");
        ArrayList<WalletItem> itemList = new ArrayList<>();
        itemList.add(walletItem1);
        itemList.add(walletItem2);
        itemList.add(walletItem3);
        itemList.add(walletItem4);
        itemList.add(walletItem5);
        walletModel.setItemsFlatList(itemList);
    }

    public void fillHashmaps(ArrayList<String> expressions){
        int counter = 0;
        for(String element : expressions){
            if(!element.equals("AND") && !element.equals("OR")){
                atomicTerms.put(counter, element);
                counter++;
            }
        }
        counter = 0;
        for(String element : expressions){
            if(element.equals("AND") || element.equals("OR")){
                booleanOperators.put(counter, element);
                counter++;
            }
        }
    }

    //Werte zunächst alle AND Ausdrücke aus und erstelle Hashsets
    public HashSet<String> filterANDExpressions(){

        for(int i = 0; i < booleanOperators.keySet().size(); i++){
            HashSet<String> currentTermSet;
            HashSet<String> nextTermSet;

            String currentAtomicTerm = atomicTerms.get(i);
            String nextAtomicTerm = atomicTerms.get(i + 1);

            //If the boolean operator is AND, get the intersection of the two terms
            if(booleanOperators.get(i).equals("AND")){
                currentTermSet = checkFlatListFor(currentAtomicTerm);
                nextTermSet = checkFlatListFor(nextAtomicTerm);
                //Schnittmenge bilden
                currentTermSet.retainAll(nextTermSet);
                //Dem resultSet hinzufügen
                HashSet<String> resultSet = new HashSet<>(currentTermSet);
                firstEvaluatedExpressions.put(i, resultSet);

            } else if (booleanOperators.get(i).equals("OR")) {
                //Randfall, wenn erster Operator OR ist, darf Term einfach ausgewertet werden
                if(i == 0){
                    currentTermSet = checkFlatListFor(currentAtomicTerm);
                    firstEvaluatedExpressions.put(i, currentTermSet);
                //Randfall, wenn letzter Operator OR ist, dann wird der letzte Term einfach ausgewertet
                } else if(i == booleanOperators.keySet().size() - 1){
                    nextTermSet = checkFlatListFor(nextAtomicTerm);
                    firstEvaluatedExpressions.put(i, nextTermSet);
                } else {
                    //Wenn der letzte Operator ein OR war, darf Term ebenfalls ausgewertet werden, denn
                    //dann steht der Term zwischen zwei OR Operatoren
                    String lastBooleanOperator = booleanOperators.get(i - 1);
                    if(lastBooleanOperator.equals("OR")){
                        currentTermSet = checkFlatListFor(currentAtomicTerm);
                        firstEvaluatedExpressions.put(i, currentTermSet);
                    }
                }
            }
        }
        System.out.println(firstEvaluatedExpressions.toString());
        return null;
    }

    public HashSet<String> evaluateExpressionHashMap(HashMap<Integer, HashSet<String>> expressionHashMap){
        ArrayList<HashSet<String>> listOfANDSets = new ArrayList<>();
        ArrayList<Integer> keyList = new ArrayList<>(expressionHashMap.keySet());

        for(int i = 0; i < keyList.size(); i++){
            HashSet<String> resultSet = new HashSet<>();

            if(i == 0){
               if(booleanOperators.get(i).equals("OR")){
                   secondEvaluatedExpressions.put(i, firstEvaluatedExpressions.get(i));
               }
            }
            //Collect AND Sets
            if(keyList.get(i + 1) - keyList.get(i) == 1){

            }
        }
        return null;
    }


    public HashSet<String> checkFlatListFor(String filter){
        HashSet<String> resultSet = new HashSet<>();

        List<WalletItem> itemList = walletModel.getItemsFlatList();

        for(WalletItem item : itemList){
            if(filter == null || item.isMatch(filter)){
                resultSet.add(item.getName());
            }
        }
        return resultSet;
    }


    public void convertStringToList(String filterTerm){
       Collections.addAll(expressions, filterTerm.split(" "));
    }


    public static void main(String[] args) {
        Filter filter = new Filter();
        filter.convertStringToList(filter.filterTerm);
        //filter.filterItems(filter.expressions, 0);
        filter.fillHashmaps(filter.expressions);
        filter.expressions.forEach(System.out::println);
        System.out.println(filter.atomicTerms.toString());
        System.out.println(filter.booleanOperators.toString());
       // HashSet<String> result = filter.filterANDExpressions();
        //System.out.println(result.toString());
       // System.out.println(filter.firstEvaluatedExpressions.toString());
        //System.out.println(filter.resultsList);
        filter.filterandExpressions();
        System.out.println("AND Expressions");
        System.out.println(filter.andExpressionsHashMap.toString());

        filter.filterOrExpressions();
        System.out.println("OR Expressions");
        System.out.println(filter.orExpressionsHashMap.toString());

        filter.buildIntersectionsOfANDSets();
        System.out.println("intersection Sets of AND sets");
        System.out.println(filter.intersectionSetHashMap.toString());


        HashSet<String> result = filter.buildUnionSets();
        System.out.println("union sets of everything");
        System.out.println(result.toString());
    }

    HashMap<Integer, ArrayList<HashSet<String>>> andExpressionsHashMap = new HashMap<>();


    //Nach dieser Methode sind alle AND Sets in einer Hashmap gespeichert, aus den jeweiligen Listen mit Hashsets
    //kann man jetzt einfach die Schnittmenge bestimmen. Dann muss man die resultierenden Sets nur noch mit
    //den Sets der OR Verknüpfung verbinden.
    public void filterandExpressions(){
        int separator = 0;
        ArrayList<HashSet<String>> listOfANDSets = new ArrayList<>();
        for(int i = 0; i < booleanOperators.keySet().size(); i++){
            HashSet<String> currentTermSet = new HashSet<>();
            HashSet<String> nextTermSet = new HashSet<>();


            String currentAtomicTerm = atomicTerms.get(i);
            String nextAtomicTerm = atomicTerms.get(i + 1);

            //If the boolean operator is AND, get the two atomicTerms to the left and right
            if(booleanOperators.get(i).equals("AND")){

                currentTermSet = checkFlatListFor(currentAtomicTerm);
                nextTermSet = checkFlatListFor(nextAtomicTerm);
                //Der Liste hinzufügen
                listOfANDSets.add(currentTermSet);
                listOfANDSets.add(nextTermSet);
            } else if (booleanOperators.get(i).equals("OR")) {
                if(listOfANDSets.size() > 0){
                    ArrayList<HashSet<String>> resultList = new ArrayList<>();
                    resultList.addAll(listOfANDSets);
                    andExpressionsHashMap.put(separator, resultList);
                    separator++;
                    listOfANDSets.clear();
                }


            }
        }
        //System.out.println(firstEvaluatedExpressions.toString());

    }

    HashMap<Integer, HashSet<String>> orExpressionsHashMap = new HashMap<>();

    public void filterOrExpressions(){
        int counter = 0;
        for(int i = 0; i < booleanOperators.keySet().size(); i++){
            HashSet<String> currentTermSet = new HashSet<>();
            HashSet<String> nextTermSet = new HashSet<>();
            String currentAtomicTerm = atomicTerms.get(i);
            String nextAtomicTerm = atomicTerms.get(i + 1);

            if(booleanOperators.get(i).equals("AND")){
                continue;

            }

            //Randfall: Wenn erster Operator OR ist, dann füge den ersten Term hinzu
            if(i == 0){
                if(booleanOperators.get(i).equals("OR")){
                    currentTermSet = checkFlatListFor(currentAtomicTerm);
                    orExpressionsHashMap.put(i, currentTermSet);
                }
            }

            //Allgemeiner Fall: Wenn nächster und letzter Operator OR ist, dann füge Term hinzu
            else if(booleanOperators.get(i).equals("OR")){
                if(booleanOperators.get(i - 1).equals("OR")){
                    currentTermSet = checkFlatListFor(currentAtomicTerm);
                    orExpressionsHashMap.put(i, currentTermSet);
                }
            }

            //Randfall: Wenn letzter Operator OR ist, dann füge den letzten Term hinzu
            if(i == booleanOperators.keySet().size() - 1){
                if(booleanOperators.get(i).equals("OR")){
                    nextTermSet = checkFlatListFor(nextAtomicTerm);
                    orExpressionsHashMap.put(i + 1, nextTermSet);
                }
            }
        }
    }

    //TODO Wenn auf beiden Seiten des Terms OR steht, dann diesen Term zu einer Hashmap hinzufügen
    //Die Sets der OR Terme können einfach vereinigt werden

    //Die Idee ist generell, dass alle AND Terme vereinfacht werden, so dass man nur noch die OR Terme vereinigen muss.

    HashMap<Integer, HashSet<String>> intersectionSetHashMap = new HashMap<>();
    public void buildIntersectionsOfANDSets(){
        int i = 0;
        for(Integer key : andExpressionsHashMap.keySet()){
            ArrayList<HashSet<String>> listOfANDSets = andExpressionsHashMap.get(key);
            HashSet<String> intersectionSet = listOfANDSets
                    .stream()
                    .skip(1)
                    .collect(() -> new HashSet<>(listOfANDSets.get(0)), Set::retainAll, Set::retainAll);

            HashSet<String> resultSet = new HashSet<>(intersectionSet);
            intersectionSetHashMap.put(i, resultSet);
            i++;
        }

    }

    //Jetzt existieren nur noch OR Verknüpfungen, daher kann man nun die Vereinigung der verbliebenen Sets bilden
    public HashSet<String> buildUnionSets(){
        HashSet<String> unionSetAND = new HashSet<>();
        HashSet<String> unionSetOR = new HashSet<>();
        HashSet<String> resultSet = new HashSet<>();

        for(Integer key : intersectionSetHashMap.keySet()){
            unionSetAND.addAll(intersectionSetHashMap.get(key));
        }

        for(Integer key : orExpressionsHashMap.keySet()){
            unionSetOR.addAll(orExpressionsHashMap.get(key));
        }

        resultSet.addAll(unionSetAND);
        resultSet.addAll(unionSetOR);

        return resultSet;
    }

}
