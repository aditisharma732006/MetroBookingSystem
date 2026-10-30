package com.metro.paymentservice.client;

import java.math.BigDecimal;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.metro.paymentservice.entity.Payment;
import com.metro.paymentservice.exception.PaymentGatewayException;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

@Component
public class RazorpayGateway {

    private final RazorpayClient client;
    private final String keySecret;
    public RazorpayGateway(@Value("${razorpay.key-id}") String keyId,
                           @Value("${razorpay.key-secret}") String keySecret) throws RazorpayException {
        this.client = new RazorpayClient(keyId, keySecret);
        this.keySecret=keySecret;
    }

    public String createOrder(Payment payment) {
        try {
            // Razorpay wants paise. longValueExact() throws if the fare has fractions of a paisa.
            long paise = payment.getAmount().multiply(BigDecimal.valueOf(100)).longValueExact();

            JSONObject options = new JSONObject();
            options.put("amount", paise);
            options.put("currency", "INR");
            options.put("receipt", "booking_" + payment.getBookingId()); // for tracing in the dashboard

            Order order = client.orders.create(options);
            return order.get("id");
        } catch (RazorpayException | ArithmeticException e) {
            throw new PaymentGatewayException("Could not create Razorpay order", e);
        }
    }
    
    

    public boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            JSONObject attrs = new JSONObject();
            attrs.put("razorpay_order_id", orderId);
            attrs.put("razorpay_payment_id", paymentId);
            attrs.put("razorpay_signature", signature);
            return com.razorpay.Utils.verifyPaymentSignature(attrs, keySecret);
        } catch (RazorpayException e) {
            return false;   // malformed input counts as invalid
        }
    }
    
}