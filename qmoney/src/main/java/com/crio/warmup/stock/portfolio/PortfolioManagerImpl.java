
package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {




  private RestTemplate restTemplate;

  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }


  //TODO: CRIO_TASK_MODULE_REFACTOR
  // 1. Now we want to convert our code into a module, so we will not call it from main anymore.
  //    Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  //    into #calculateAnnualizedReturn function here and ensure it follows the method signature.
  // 2. Logic to read Json file and convert them into Objects will not be required further as our
  //    clients will take care of it, going forward.

  // Note:
  // Make sure to exercise the tests inside PortfolioManagerTest using command below:
  // ./gradlew test --tests PortfolioManagerTest

  //CHECKSTYLE:OFF
  public static AnnualizedReturn calculateAnnualizedReturns(PortfolioTrade trade,LocalDate endDate,
  Double buyPrice, Double sellPrice) {
  double totalReturn = (sellPrice - buyPrice) / buyPrice;
  double yearsCount=ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate)/365.24;
  double annualizedReturns=Math.pow((1 + totalReturn),(1 / yearsCount))-1;    
  return new AnnualizedReturn(trade.getSymbol(), annualizedReturns, totalReturn);
}



  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo third-party APIs to a separate function.
  //  Remember to fill out the buildUri function and use that.


  // public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
  //     throws JsonProcessingException {
  //    return null;
  // }
  public List<Candle> getStockQuote(String symbol,LocalDate from, LocalDate to) {
    //return Collections.emptyList();
    RestTemplate rtemplate=new RestTemplate();
    String url=buildUri(symbol,from,to);
    TiingoCandle[] candles=rtemplate.getForObject(url, TiingoCandle[].class);
    return Arrays.asList(candles);
 }
 public static String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
  //return Collections.emptyList();
  String url="https://api.tiingo.com/tiingo/daily/"+symbol+"/prices?startDate="+startDate+"&endDate="+endDate+"&token="+"61682185e62b65957744b1db9b148a8a97022b0a";
  return url;
}

  // protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
  //      String uriTemplate = "https:api.tiingo.com/tiingo/daily/$SYMBOL/prices?"
  //           + "startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKEY";
  // }


  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate) {
    // TODO Auto-generated method stub
    List<Candle> list=new ArrayList<Candle>();
    List<AnnualizedReturn> annualizedReturnList = new ArrayList<>();
    for(PortfolioTrade t : portfolioTrades){
      list=getStockQuote(t.getSymbol(), t.getPurchaseDate(), endDate);
      AnnualizedReturn annualizedReturn= calculateAnnualizedReturns(t,endDate,list.get(0).getOpen(),list.get(list.size()-1).getClose());
      // AnnualizedReturn annualizedReturn= calculateAnnualizedReturns(LocalDate.parse(args[1]),trade,l.get(0).getOpen(),l.get(l.size()-1).getClose());
      annualizedReturnList.add(annualizedReturn);
    }
    Collections.sort(annualizedReturnList, new Comparator<AnnualizedReturn>(){
      public int compare(AnnualizedReturn t1, AnnualizedReturn t2){
        return (int)(t2.getAnnualizedReturn().compareTo(t1.getAnnualizedReturn()));
    }
    });
 return annualizedReturnList;
  }
}
