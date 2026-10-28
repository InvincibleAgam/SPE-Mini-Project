package com.spe.calculator.controller;

import com.spe.calculator.model.CalculationResult;
import com.spe.calculator.service.CalculatorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class CalculatorController {

    private final CalculatorService calculator;

    public CalculatorController(CalculatorService calculator) {
        this.calculator = calculator;
    }

    @GetMapping("/add")
    public CalculationResult add(@RequestParam double a, @RequestParam double b) {
        return calculator.add(a, b);
    }

    @GetMapping("/subtract")
    public CalculationResult subtract(@RequestParam double a, @RequestParam double b) {
        return calculator.subtract(a, b);
    }

    @GetMapping("/multiply")
    public CalculationResult multiply(@RequestParam double a, @RequestParam double b) {
        return calculator.multiply(a, b);
    }

    @GetMapping("/divide")
    public CalculationResult divide(@RequestParam double a, @RequestParam double b) {
        return calculator.divide(a, b);
    }
}
