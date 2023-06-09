package com.example.foodsellingapp.controller;

import com.example.foodsellingapp.model.eenum.StatusDelivery;
import com.example.foodsellingapp.service.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {
    @Autowired
    DeliveryService deliveryService;

    @CrossOrigin(origins = "*", allowedHeaders = "*", methods = RequestMethod.GET)
    @GetMapping("/get-all")
    public ResponseEntity<?> getDeliveryByData(){
        return ResponseEntity.ok(deliveryService.getAll());
    }


    @CrossOrigin(origins = "*", allowedHeaders = "*", methods = RequestMethod.POST)
    @PostMapping("/create-delivery")
    public ResponseEntity<?> createDelivery(@RequestParam Long shipperId,@RequestParam Long orderId){
        return ResponseEntity.ok(deliveryService.getShipper(shipperId,orderId));
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateDelivery(@RequestParam Long orderId, @RequestParam(name = "status") StatusDelivery statusDelivery){
        return ResponseEntity.ok(deliveryService.updateDelivery(orderId,statusDelivery));
    }
}
