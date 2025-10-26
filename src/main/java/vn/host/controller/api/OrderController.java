package vn.host.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.host.entity.Payment;
import vn.host.entity.User;
import vn.host.model.request.AddressRequest;
import vn.host.model.request.OrderRequest;
import vn.host.model.request.ShippingFeeRequest;
import vn.host.model.response.OrderItemResponse;
import vn.host.model.response.OrderResponse;
import vn.host.model.response.ResponseModel;
import vn.host.model.response.TempOrderResponse;
import vn.host.repository.OrderRepository;
import vn.host.service.OrderItemService;
import vn.host.service.OrderService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    OrderService orderService;

    @Autowired
    OrderItemService orderItemService;

    @GetMapping("{id}")
    public ResponseEntity<?> getOrderByUserId(@PathVariable Long id){
        try {
            List<OrderResponse> orders = orderService.getOrdersByUserId(id);
            return new ResponseEntity<>(
                    new ResponseModel(
                            "Success",
                            "Orders retrieved successfully",
                            orders
                    ), HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ResponseModel(
                            "Error",
                            "Failed to retrieve orders: " + e.getMessage(),
                            null
                    ), HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @PostMapping()
    public ResponseEntity<?> saveOrder(@RequestBody OrderRequest orderRequest){
        try {
            orderService.saveOrder(orderRequest);
            return new ResponseEntity<>(
                    new ResponseModel(
                            "Success",
                            "Order saved successfully",
                            null
                    ), HttpStatus.CREATED
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ResponseModel(
                            "Error",
                            "Failed to save order: " + e.getMessage(),
                            null
                    ), HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @PutMapping("status")
    public ResponseEntity<?> updateStatus(@RequestParam("order") Long id, @RequestParam("status") String status){
        try {
            orderService.updateStatus(id, status);
            return new ResponseEntity<>(
                    new ResponseModel(
                            "Success",
                            "Order status updated successfully",
                            null
                    ), HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ResponseModel(
                            "Error",
                            "Failed to update order status: " + e.getMessage(),
                            null
                    ), HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("{userId}/{orderId}")
    public ResponseEntity<?> getOrderDetail(@PathVariable Long userId, @PathVariable Long orderId){
        try {
            List<OrderItemResponse> orderDetails = orderItemService.getOrderDetailsByOrderIdAndUserId(userId, orderId);
            return new ResponseEntity<>(
                    new ResponseModel(
                            "Success",
                            "Order detail retrieved successfully",
                            null
                    ), HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ResponseModel(
                            "Error",
                            "Failed to retrieve order detail: " + e.getMessage(),
                            null
                    ), HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @PutMapping("payment")
    public ResponseEntity<?> updatePayment(@RequestParam("order") Long id, @RequestBody Payment payment){
        try {
            orderService.updatePayment(id, payment);
            return new ResponseEntity<>(
                    new ResponseModel(
                            "Success",
                            "Order status updated successfully",
                            null
                    ), HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ResponseModel(
                            "Error",
                            "Failed to update order payment: " + e.getMessage(),
                            null
                    ), HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @PostMapping("shipping-fee")
    public ResponseEntity<?> calculateShippingFee(@RequestBody ShippingFeeRequest shippingFeeRequest){
        try {
            Double fee = orderService.calculateShippingFee(shippingFeeRequest);
            return new ResponseEntity<>(
                    new ResponseModel(
                            "Success",
                            "Shipping fee calculated successfully",
                            fee
                    ), HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ResponseModel(
                            "Error",
                            "Failed to calculate shipping fee: " + e.getMessage(),
                            null
                    ), HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

    }

    @PostMapping("temp-order")
    public ResponseEntity<?> saveTempOrder(@RequestBody Object order, HttpSession session){
        try {
            session.removeAttribute("selectedProducts");
            Map<String, Object> orderMap = (Map<String, Object>) order;
            ObjectMapper mapper = new ObjectMapper();
            TempOrderResponse tempOrderResponse = mapper.convertValue(orderMap, TempOrderResponse.class);
            String orderId = orderService.findTopOrderByUser(tempOrderResponse);
            orderMap.put("orderCode", orderId);
            session.setAttribute("tempOrder", order);
            return new ResponseEntity<>(
                    new ResponseModel(
                            "Success",
                            "Orders saved successfully",
                            order
                    ), HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ResponseModel(
                            "Error",
                            "Failed to saved orders: " + e.getMessage(),
                            null
                    ), HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("temp-order")
    public ResponseEntity<?> getTempOrder(HttpSession session) {
        try {
            Map<String, Object> order = (Map<String, Object>) session.getAttribute("tempOrder");
            return new ResponseEntity<>(
                    new ResponseModel(
                            "Success",
                            "Orders retrieved successfully",
                            order
                    ), HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ResponseModel(
                            "Error",
                            "Failed to retrieve orders: " + e.getMessage(),
                            null
                    ), HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
