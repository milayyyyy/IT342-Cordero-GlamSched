package edu.cit.cordero.glamsched.features.payment;

import edu.cit.cordero.glamsched.shared.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentRepository paymentRepository;

    public PaymentController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @GetMapping
    public ApiResponse<List<Payment>> getPayments(@RequestParam Long clientId) {
        return ApiResponse.success(paymentRepository.findByClientId(clientId));
    }

    @PostMapping
    public ApiResponse<Payment> processPayment(@RequestBody Payment payment) {
        payment.setStatus("COMPLETED");
        return ApiResponse.success(paymentRepository.save(payment));
    }
}
