package org.mhisoft.wallet.view;

import org.mhisoft.wallet.model.ItemType;
import org.mhisoft.wallet.model.WalletItem;
import org.mhisoft.wallet.model.WalletModel;
import org.mhisoft.wallet.service.ServiceRegistry;

import java.util.*;

public class FilterParser {

    // filterTerm = "Photoshop AND Steam OR EPIC GAMES";

    //String filterTerm = "Photoshop AND Steam OR Sparkasse";

    String filterTerm = "Steam OR Spar AND P OR Com AND E AND B AND E OR Spar AND P OR Photo OR Commerz OR E OR P AND Epic Games reloaded";

    String filterTermTest = "Steam OR (Spar AND P) OR ((Com AND E) AND B)) AND Epic) OR Spar AND P OR Photo";

    String filterTerm2 = "Photo OR S AND T ";

    String filterTerm3 = "H OR K AND S OR C";

    WalletModel walletModel = ServiceRegistry.instance.getWalletModel();
    ArrayList<String> expressions = new ArrayList<>();
    HashMap<Integer, String> atomicTerms = new HashMap<>();
    HashMap<Integer, String> booleanOperators = new HashMap<>();

    HashMap<Integer, ArrayList<HashSet<String>>> andExpressionsHashMap = new HashMap<>();
    HashMap<Integer, HashSet<String>> orExpressionsHashMap = new HashMap<>();
    HashMap<Integer, HashSet<String>> intersectionSetHashMap = new HashMap<>();

    public static void main(String[] args) {
        FilterParser filterParser = new FilterParser();
        filterParser.convertStringToList(filterParser.filterTerm);

        filterParser.fillHashmaps(filterParser.expressions);
        filterParser.expressions.forEach(System.out::println);
        System.out.println(filterParser.atomicTerms.toString());
        System.out.println(filterParser.booleanOperators.toString());

        filterParser.filterANDExpressions();
        System.out.println("AND Expressions");
        System.out.println(filterParser.andExpressionsHashMap.toString());

        filterParser.filterORExpressions();
        System.out.println("OR Expressions");
        System.out.println(filterParser.orExpressionsHashMap.toString());

        filterParser.buildIntersectionsOfANDSets();
        System.out.println("intersection Sets of AND sets");
        System.out.println(filterParser.intersectionSetHashMap.toString());


        HashSet<String> result = filterParser.buildUnionSets();
        System.out.println("union sets of everything");
        System.out.println(result.toString());
    }



    public FilterParser(){
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
        Collections.addAll(expressions, filterTerm.trim().split(" AND | OR "));
        List<String> list = new ArrayList<>();
        list.add("test");
        list.add("Hallo");
        list.add(" AND ");
        list.add("OR");
        System.out.println(list.toString());
        System.out.println("expressions " + expressions.toString());
    }







    //Nach dieser Methode sind alle AND Sets in einer Hashmap gespeichert, aus den jeweiligen Listen mit Hashsets
    //kann man jetzt einfach die Schnittmenge bestimmen. Dann muss man die resultierenden Sets nur noch mit
    //den Sets der OR Verknüpfung verbinden.

    //Filtere die AND-verknüpften Terme in eine Hashmap
    public void filterANDExpressions(){
        int separator = 0;
        ArrayList<HashSet<String>> listOfANDSets = new ArrayList<>();
        for(int i = 0; i < booleanOperators.keySet().size(); i++){
            HashSet<String> currentTermSet;
            HashSet<String> nextTermSet;


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
                    ArrayList<HashSet<String>> resultList = new ArrayList<>(listOfANDSets);
                    andExpressionsHashMap.put(separator, resultList);
                    separator++;
                    listOfANDSets.clear();
                }
            }
        }
    }


    //Filtere die OR-verknüpften Terme in eine Hashmap
    public void filterORExpressions(){
        for(int i = 0; i < booleanOperators.keySet().size(); i++){
            HashSet<String> currentTermSet;
            HashSet<String> nextTermSet;
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

    //Die Idee ist generell, dass alle AND Terme vereinfacht werden, sodass man nur noch die OR Terme vereinigen muss.

    //Aus den gefilterten AND-Termen wird jeweils die Schnittmenge gebildet
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
