package com.cryptox.controller; 
import com.cryptox.dto.*;
 import com.cryptox.service.TradeOrchestrator;
  import jakarta.servlet.http.HttpServletRequest;
  import jakarta.validation.Valid;
 import lombok.RequiredArgsConstructor;
  import org.springframework.http.*; 
  import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController
 @RequestMapping("/api/trade")
  @RequiredArgsConstructor public class TradeController{final TradeOrchestrator s; String u(HttpServletRequest r){Object x=r.getAttribute("username");if(x==null)throw new SecurityException();return x.toString();} @GetMapping("/supported-coins") Object coins(){return s.supported();} @GetMapping("/quote/{c}") Object quote(@PathVariable String c){return new QuoteResponse(c.toUpperCase(),s.price(c));} @PostMapping("/buy") ResponseEntity<?> buy(HttpServletRequest q,@Valid @RequestBody BuyRequest r){return run(()->s.buy(u(q),r));} @PostMapping("/sell") ResponseEntity<?> sell(HttpServletRequest q,@Valid @RequestBody SellRequest r){return run(()->s.sell(u(q),r));} @PostMapping("/swap") ResponseEntity<?> swap(HttpServletRequest q,@Valid @RequestBody SwapRequest r){return run(()->s.swap(u(q),r));} ResponseEntity<?> run(java.util.concurrent.Callable<?> c){try{return ResponseEntity.ok(c.call());}catch(SecurityException e){return ResponseEntity.status(401).build();}catch(Exception e){String m=e.getMessage()==null?"Trade failed":e.getMessage();return ResponseEntity.badRequest().body(Map.of("message",m));}}}