package com.example.LoginCompleto.home.controller;

import com.example.LoginCompleto.item.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ItemService itemService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("totalItems", itemService.count());
        model.addAttribute("lowStockItems", itemService.countLowStock());
        return "home/index";
    }
}
