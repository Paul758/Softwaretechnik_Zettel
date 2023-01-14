package org.mhisoft.wallet.view;

import org.mhisoft.wallet.model.ItemType;
import org.mhisoft.wallet.model.WalletItem;
import org.mhisoft.wallet.model.WalletModel;
import org.mhisoft.wallet.service.ServiceRegistry;

import java.util.*;

public class Filter {


    // filterTerm = "Photoshop AND Steam OR EPIC GAMES";

    //String filterTerm = "Photoshop AND Steam OR Sparkasse";

    String filterTerm = "Steam OR Spar AND P OR Com AND E AND B AND Epic OR Spar AND P OR Photo";

    String filterTermTest = "Steam OR (Spar AND P) OR ((Com AND E) AND B)) AND Epic) OR Spar AND P OR Photo";

    String filterTerm2 = "Photo OR S AND T ";

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
            //System.out.println(element);
            //System.out.println("the element" + element + "does not equal AND");
            if(element.equals("AND") || element.equals("OR")){
                booleanOperators.put(counter, element);
                counter++;
            }
        }
    }

    //Werte zunächst alle AND Ausdrücke aus und erstelle Hashsets
    public HashSet<String> filterANDExpressions(){

        HashSet<String> intersectionSet = new HashSet<>();
        int counter = 0;
        for(int i = 0; i < booleanOperators.keySet().size(); i++){
            HashSet<String> resultSet = new HashSet<>();
            HashSet<String> currentTermSet = new HashSet<>();
            HashSet<String> nextTermSet = new HashSet<>();

            resultSet.clear();
            currentTermSet.clear();
            nextTermSet.clear();

            String currentAtomicTerm = atomicTerms.get(i);
            String nextAtomicTerm = atomicTerms.get(i + 1);
            System.out.println(i);
            //If the boolean operator is AND, get the intersection of the two terms
            if(booleanOperators.get(i).equals("AND")){
                currentTermSet = checkFlatListFor(currentAtomicTerm);
                nextTermSet = checkFlatListFor(nextAtomicTerm);

                //Schnittmenge bilden
                currentTermSet.retainAll(nextTermSet);

                //Dem resultSet hinzufügen
                resultSet.addAll(currentTermSet);
                //System.out.println(resultSet.toString());
                //Der evaluated Expression HashMap hinzufügen
                System.out.println("now setting" + resultSet.toString() + "at index" + counter);
                firstEvaluatedExpressions.put(counter, resultSet);
                System.out.println(firstEvaluatedExpressions.toString());

            } else if (booleanOperators.get(i).equals("OR")) {
                //Randfall, wenn erster Operator OR ist, darf Term einfach ausgewertet werden
                if(i == 0){
                    currentTermSet = checkFlatListFor(currentAtomicTerm);
                    System.out.println("now setting" + currentTermSet.toString() + "at index" + i);
                    firstEvaluatedExpressions.put(i, currentTermSet);
                    //continue;

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
                        System.out.println("now setting" + currentTermSet.toString() + "at index" + i);
                        firstEvaluatedExpressions.put(i, currentTermSet);
                        continue;
                    }
                }
               // counter--;
            }
            counter++;
        }
        System.out.println(firstEvaluatedExpressions.toString());
        return null;
    }

    public HashSet<String> evaluateExpressionHashMap(HashMap<Integer, HashSet<String>> expressionHashMap){
        ArrayList<Integer> keyList = new ArrayList<>();
        ArrayList<HashSet<String>> listOfANDSets = new ArrayList<>();
        for(Integer key : expressionHashMap.keySet()){
            keyList.add(key);
        }

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


   /* public boolean filterItems(ArrayList<String> expressions, int index){
        boolean boolValue;
        System.out.println("index is:" + index);
        System.out.printf("current item is" + expressions.get(index));
        if(index + 2 < expressions.size()){
            if(expressions.get(index + 1).equals("AND")){
                System.out.println("checking" + expressions.get(index));
                boolValue = checkFlatListFor(expressions.get(index)) & filterItems(expressions, index + 2);
                System.out.println(boolValue);
                return boolValue;
            } else{
                System.out.println("checking" + expressions.get(index));
                boolValue = checkFlatListFor(expressions.get(index)) | filterItems(expressions, index + 2);
                System.out.println(boolValue);
                return boolValue;
            }
        } else {
            System.out.println("index is:" + index);
            System.out.printf("current item is" + expressions.get(expressions.size() - 1));
            return checkFlatListFor(expressions.get(expressions.size() - 1));
        }
    }*/





    /*public boolean checkFlatListFor(String filter){
        boolean itemFound = false;
        List<WalletItem> itemList = walletModel.getItemsFlatList();
        System.out.println(itemList);
        for(WalletItem item : itemList){
            if(filter == null || item.isMatch(filter)){
                resultsList.add(item);
                System.out.println("item found" + item);
                itemFound = true;
            }
        }
        System.out.println(resultsList);
        return itemFound;
    }*/




    public static void main(String[] args) {
        Filter filter = new Filter();
        filter.convertStringToList(filter.filterTerm);
        //filter.filterItems(filter.expressions, 0);
        filter.fillHashmaps(filter.expressions);
        filter.expressions.forEach(s -> System.out.println(s));
        System.out.println(filter.atomicTerms.toString());
        System.out.println(filter.booleanOperators.toString());
        HashSet<String> result = filter.filterANDExpressions();
        //System.out.println(result.toString());
        System.out.println(filter.firstEvaluatedExpressions.toString());
        //System.out.println(filter.resultsList);
    }
}
