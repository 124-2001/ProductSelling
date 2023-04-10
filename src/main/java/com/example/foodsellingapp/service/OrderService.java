package com.example.foodsellingapp.service;

import com.example.foodsellingapp.model.dto.OrderDetailDTO;
import com.example.foodsellingapp.model.eenum.StatusOrder;
import com.example.foodsellingapp.model.entity.Order;
import com.example.foodsellingapp.model.entity.OrdersDetail;
import com.example.foodsellingapp.repository.OrdersDetailRepository;
import com.example.foodsellingapp.repository.ProductRepository;
import com.example.foodsellingapp.repository.OrderRepository;
import com.example.foodsellingapp.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.List;

@Service
@Transactional
@Slf4j
public class OrderService {
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    OrdersDetailRepository orderDetailRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    UserRepository userRepository;

//    @Value("${DISTANCE_MATRIX_API_URL}")
//    private String DISTANCE_MATRIX_API_URL;
//
//    @Value("${API_KEY}")
//    private String API_KEY;
//
//    @Value("${address}")
//    private String address;
    private static final String DISTANCE_MATRIX_API_URL = "https://maps.googleapis.com/maps/api/distancematrix/json";
    private static final String API_KEY = "AIzaSyDp7J3q2UIA4WvUqWLe--QinPej2JSnrw8";
    private static final String address ="814 láng";


    public List<Order> getAll(){
        return orderRepository.findAll();
    }

    public Order createOrder(List<OrderDetailDTO> dtos, Long customerId) throws IOException {
//        ModelMapper mapper = new ModelMapper();
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        Order order  = new Order();
        order.setCustomerId(customerId);
        order.setCreatedDate(currentTimestamp);
        order.setStatusOrder(StatusOrder.WAITING);
        orderRepository.save(order);
        if(dtos.isEmpty()){
            throw new RuntimeException("Order is empty");
        }
        double totalPrice=0;
        for (OrderDetailDTO dto : dtos) {
            //OrderDetail orderDetail = mapper.map(dto,OrderDetail.class);
            OrdersDetail orderDetail = new OrdersDetail();
            orderDetail.setProductId(dto.getProductId());
            orderDetail.setQuantity(dto.getQuantity());
            orderDetail.setOrderId(order.getId());
            orderDetail.setPrice(productRepository.findById(dto.getProductId()).get().getPrice());
            totalPrice+=dto.getQuantity()* productRepository.findById(dto.getProductId()).get().getPrice();
            orderDetailRepository.save(orderDetail);
        }
        //lấy tiền vận chuyển theo khoảng cách
        double deliveryPrice = getDeliveryPrice(userRepository.findById(customerId).get().getAddress());
        totalPrice+=deliveryPrice;
        order.setTotalPrice(totalPrice);
        return orderRepository.save(order);
    }

    public void deleteOrder(Long orderId){
        Order order= orderRepository.findById(orderId).get();
        if(order.getStatusOrder()!= StatusOrder.WAITING){
            throw new  RuntimeException("Order is APPROVE . Can't cancel order");
        }
        List<OrdersDetail> orderDetails = orderDetailRepository.findAllByOrderId(orderId);
        if(!orderDetails.isEmpty()){
            orderDetailRepository.deleteAll(orderDetails);
        }
        orderRepository.delete(order);
    }

    public void updateOrder(long orderId,List<OrderDetailDTO> dtos){
        Order order= orderRepository.findById(orderId).get();
        ModelMapper mapper = new ModelMapper();
        if(order.getStatusOrder()== StatusOrder.WAITING){
            orderDetailRepository.deleteAll(orderDetailRepository.findAllByOrderId(orderId));
            for (OrderDetailDTO dto : dtos) {
                OrdersDetail detail = mapper.map(dto,OrdersDetail.class);
                detail.setOrderId(orderId);
                orderDetailRepository.save(detail);
            }
        }
        else {
            throw new  RuntimeException("Order is APPROVE or REJECT . Can't update order");
        }
    }

    public void feedbackOrder(long orderDetailId,String feedback){
        OrdersDetail orderDetail = orderDetailRepository.findById(orderDetailId).orElseThrow(()->
                new RuntimeException("The product is not included in the order"));
        orderDetail.setFeedBack(feedback);
    }

    public boolean approveOrder(long orderId){
        Order order= orderRepository.findById(orderId).get();
        if(order.getStatusOrder()==StatusOrder.WAITING){
            order.setStatusOrder(StatusOrder.APPROVED);
            return true;
        }
        return false;
    }

    public double getDeliveryPrice(String destination) throws IOException {
        double distance = getDistance(destination);
        System.out.println(distance);
        if(distance<= 2.5){
            return 0;
        }
        else if (2.5<distance && distance<=5){
            return 25;
        }
        else if (distance>20){
            return 40;
        }
        else {
            return 5000*distance;
        }
    }

    public double getDistance(String destination) throws IOException {
        String url = DISTANCE_MATRIX_API_URL + "?origins=" + URLEncoder.encode(address, "UTF-8")
                + "&destinations=" + URLEncoder.encode(destination, "UTF-8")
                + "&key=" + API_KEY;

        URL urlObject = new URL(url);

        try (BufferedReader in = new BufferedReader(new InputStreamReader(urlObject.openStream()))) {
            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            JSONObject jsonObject = new JSONObject(response.toString());

            String status = jsonObject.getString("status");

            if (!"OK".equals(status)) {
                throw new IOException("API returned status code " + status);
            }

            JSONObject element = jsonObject.getJSONArray("rows").getJSONObject(0)
                    .getJSONArray("elements").getJSONObject(0);
//            String distanceText = element.getJSONObject("distance").getString("text");
            double distanceValue = element.getJSONObject("distance").getDouble("value") / 1000.0;

            return distanceValue;
        } catch (JSONException e) {
            e.printStackTrace();
            return 0;
        }
    }



}
