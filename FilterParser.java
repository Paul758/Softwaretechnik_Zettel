package org.mhisoft.wallet.view;

import org.mhisoft.wallet.model.ItemType;
import org.mhisoft.wallet.model.WalletItem;
import org.mhisoft.wallet.model.WalletModel;
import org.mhisoft.wallet.service.ServiceRegistry;

import java.lang.reflect.Array;
import java.util.*;

public class FilterParser {

    // filterTerm = "Photoshop AND Steam OR EPIC GAMES";

    //String filterTerm = "Photoshop AND Steam OR Sparkasse";

    String filterTerm = "Steam OR Spar AND P OR Com AND E AND B AND E OR Spar AND P OR Photo OR Commerz OR E OR P AND Epic Games reloaded";

    String filterTermTest = "Steam OR (Spar AND P) OR ((Com AND E) AND B)) AND Epic) OR Spar AND P OR Photo";

    String filterTerm2 = "Photo OR S AND T";

    String filterTerm3 = "H OR K AND S OR C";

    String filterTerm4 = "Photo";

    String filterTerm5 = "GIT OR J AND LL OR Ilias AND Uni Marburg OR Spar";

    WalletModel walletModel = ServiceRegistry.instance.getWalletModel();
    ArrayList<String> expressions = new ArrayList<>();
    HashMap<Integer, AtomicFilter> atomicTerms = new HashMap<>();
    HashMap<Integer, AtomicFilter> booleanOperators = new HashMap<>();
    HashMap<Integer, ArrayList<HashSet<String>>> andExpressionsHashMap = new HashMap<>();
    HashMap<Integer, HashSet<String>> orExpressionsHashMap = new HashMap<>();
    HashMap<Integer, HashSet<String>> intersectionSetHashMap = new HashMap<>();

    ListExplorerView listExplorerView = ServiceRegistry.instance.getWalletForm().listExploreView;

    ArrayList<String> terms = new ArrayList<>();
    ArrayList<String> operators = new ArrayList<>();

    public static void main(String[] args) {
        FilterParser filterParser = new FilterParser();
        filterParser.convertStringToList(filterParser.filterTerm5);

        filterParser.fillHashmaps();
        filterParser.expressions.forEach(System.out::println);

        System.out.println("atomic terms");
        for(int i = 0; i < filterParser.atomicTerms.keySet().size(); i++){
            System.out.println(filterParser.atomicTerms.get(i).getExpression());
        }

        System.out.println("operators");
        for(int i = 0; i < filterParser.booleanOperators.keySet().size(); i++){
            System.out.println(filterParser.booleanOperators.get(i).getExpression());
        }

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


    public ArrayList<String> parseTerm(String filterExpression){
        convertStringToList(filterExpression);
        if(expressions.size() == 1){
            return expressions;
        }
        fillHashmaps();
        filterANDExpressions();
        filterORExpressions();
        buildIntersectionsOfANDSets();
        HashSet<String> resultSet = buildUnionSets();
        //Get WalletItems
        ArrayList<String> filteredItems = new ArrayList<>(resultSet);

        //Collections.addAll(filteredItems, resultSet);

        /*for(String element : resultSet){
            walletModel.getItemsFlatList().forEach(item -> {
                if (item.isMatch(element)) {
                    filteredItems.add(item);
                }
            });
        }*/
        return filteredItems;
    }


    public FilterParser(){
        WalletItem item1 = new WalletItem(ItemType.item, "Ilias Uni Marburg");
        WalletItem item2 = new WalletItem(ItemType.item, "GitHub");
        WalletItem item3 = new WalletItem(ItemType.item, "IntelliJ");
        WalletItem item4 = new WalletItem(ItemType.item, "Sparkasse");
        WalletItem item5 = new WalletItem(ItemType.item, "Adobe Photoshop");

        /*WalletItem walletItem1 = new WalletItem(ItemType.item, "Photoshop");
        WalletItem walletItem2 = new WalletItem(ItemType.item, "Steam");
        //WalletItem walletItem3 = new WalletItem(ItemType.item, "Epic Games");
        WalletItem walletItem3 = new WalletItem(ItemType.item, "Sparkasse");
        WalletItem walletItem4 = new WalletItem(ItemType.item, "Commerzbank");
        WalletItem walletItem5 = new WalletItem(ItemType.item, "Epic Games");*/
        ArrayList<WalletItem> itemList = new ArrayList<>();
        itemList.add(item1);
        itemList.add(item2);
        itemList.add(item3);
        itemList.add(item4);
        itemList.add(item5);
        walletModel.setItemsFlatList(itemList);
    }

    //Atomic Terms und Operatoren werden in Hashmaps gespeichert
    public void fillHashmaps(){
        //Put terms in hashmap
        for(int i = 0; i < terms.size(); i++){
            atomicTerms.put(i, new AtomicFilter(terms.get(i), false));
        }
        //put operators in Hashmap
        for(int i = 0; i < operators.size(); i++){
            booleanOperators.put(i, new AtomicFilter(operators.get(i), true));
        }
    }

    //Filter items based on a singular atomic filter
    public HashSet<String> checkFlatListFor(AtomicFilter filter){
        HashSet<String> resultSet = new HashSet<>();

        List<WalletItem> itemList = walletModel.getItemsFlatList();

        //Methode des FilterExpression Interface zum filtern verwenden
        for(WalletItem item : itemList){
            if(filter == null || filter.matches(item)){
                resultSet.add(item.getName());
            }
        }
        return resultSet;
    }

    public void convertStringToList(String filterTerm){
        ArrayList<String> atomTerms = new ArrayList<>();
        Collections.addAll(atomTerms, filterTerm.trim().split(" AND | OR "));
        System.out.println("atom terms are ");
        System.out.println(atomTerms.toString());

        Collections.addAll(expressions, filterTerm.trim().split(" "));
        for(String element : expressions){
            if(element.equals("AND") || element.equals("OR")){
                operators.add(element);
            } else {
                //terms.add(element);
            }
        }
        terms = atomTerms;
    }


    public void filterANDExpressions(){
        int separator = 0;
        ArrayList<HashSet<String>> listOfANDSets = new ArrayList<>();
        for(int i = 0; i < booleanOperators.keySet().size(); i++){
            HashSet<String> currentTermSet;
            HashSet<String> nextTermSet;

            AtomicFilter currentAtomicTerm = atomicTerms.get(i);
            AtomicFilter nextAtomicTerm = atomicTerms.get(i + 1);

            //If the boolean operator is AND, get the two atomicTerms to the left and right
            if(booleanOperators.get(i).getExpression().equals("AND")){

                currentTermSet = checkFlatListFor(currentAtomicTerm);
                nextTermSet = checkFlatListFor(nextAtomicTerm);
                //Der Liste hinzufügen
                listOfANDSets.add(currentTermSet);
                listOfANDSets.add(nextTermSet);
                System.out.println(listOfANDSets.toString());
            } else if (booleanOperators.get(i).getExpression().equals("OR")) {
                if(listOfANDSets.size() > 0){
                    ArrayList<HashSet<String>> resultList = new ArrayList<>(listOfANDSets);
                    andExpressionsHashMap.put(separator, resultList);
                    separator++;
                    listOfANDSets.clear();
                }
            } if (i == booleanOperators.keySet().size() -1){
                if(listOfANDSets.size() > 0){
                    ArrayList<HashSet<String>> resultList = new ArrayList<>(listOfANDSets);
                    andExpressionsHashMap.put(separator, resultList);
                    separator++;
                    listOfANDSets.clear();
                }
            }
        }
    }

    //Nach dieser Methode sind alle AND Sets in einer Hashmap gespeichert, aus den jeweiligen Listen mit Hashsets
    //kann man jetzt einfach die Schnittmenge bestimmen. Dann muss man die resultierenden Sets nur noch mit
    //den Sets der OR Verknüpfung verbinden.

    //Filtere die AND-verknüpften Terme in eine Hashmap


    //Filtere die OR-verknüpften Terme in eine Hashmap
    public void filterORExpressions(){
        for(int i = 0; i < booleanOperators.keySet().size(); i++){
            HashSet<String> currentTermSet;
            HashSet<String> nextTermSet;
            AtomicFilter currentAtomicTerm = atomicTerms.get(i);
            AtomicFilter nextAtomicTerm = atomicTerms.get(i + 1);

            if(booleanOperators.get(i).getExpression().equals("AND")){
                continue;
            }

            //Randfall: Wenn erster Operator OR ist, dann füge den ersten Term hinzu
            if(i == 0){
                if(booleanOperators.get(i).getExpression().equals("OR")){
                    currentTermSet = checkFlatListFor(currentAtomicTerm);
                    orExpressionsHashMap.put(i, currentTermSet);
                }
            }

            //Allgemeiner Fall: Wenn nächster und letzter Operator OR ist, dann füge Term hinzu
            else if(booleanOperators.get(i).getExpression().equals("OR")){
                if(booleanOperators.get(i - 1).getExpression().equals("OR")){
                    currentTermSet = checkFlatListFor(currentAtomicTerm);
                    orExpressionsHashMap.put(i, currentTermSet);
                }
            }

            //Randfall: Wenn letzter Operator OR ist, dann füge den letzten Term hinzu
            if(i == booleanOperators.keySet().size() - 1){
                System.out.println("randfall OR");
                if(booleanOperators.get(i).getExpression().equals("OR")){
                    System.out.println("called");
                    nextTermSet = checkFlatListFor(nextAtomicTerm);
                    System.out.println("nextTermset is" + nextTermSet.toString());
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
