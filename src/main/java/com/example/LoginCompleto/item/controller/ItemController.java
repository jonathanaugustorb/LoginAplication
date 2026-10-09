package com.example.LoginCompleto.item.controller;

import com.example.LoginCompleto.common.exception.BusinessException;
import com.example.LoginCompleto.common.web.FlashMessage;
import com.example.LoginCompleto.common.web.PageUrlBuilder;
import com.example.LoginCompleto.infra.security.Permissions;
import com.example.LoginCompleto.item.dto.ItemDTO;
import com.example.LoginCompleto.item.dto.ItemFilterDTO;
import com.example.LoginCompleto.item.dto.StockEntryDTO;
import com.example.LoginCompleto.item.entity.Item;
import com.example.LoginCompleto.item.entity.enums.ItemCategory;
import com.example.LoginCompleto.item.mapper.ItemMapper;
import com.example.LoginCompleto.item.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(ItemController.BASE_PATH)
@RequiredArgsConstructor
public class ItemController {

    static final String BASE_PATH = "/items";

    private static final String LIST_VIEW = "item/list";
    private static final String FORM_VIEW = "item/form";
    private static final String DETAIL_VIEW = "item/detail";
    private static final String REDIRECT_BASE = "redirect:" + BASE_PATH;

    private final ItemService itemService;
    private final ItemMapper itemMapper;

    @ModelAttribute("categories")
    public ItemCategory[] categories() {
        return ItemCategory.values();
    }

    @GetMapping
    public String list(@ModelAttribute("filter") ItemFilterDTO filter,
                       @PageableDefault(size = 10, sort = "name") Pageable pageable,
                       Model model) {
        model.addAttribute("page", itemService.search(filter, pageable));
        model.addAttribute("pageUrl", PageUrlBuilder.build(BASE_PATH, filter.toQueryParams(), pageable.getSort()));
        model.addAttribute("currentSort", PageUrlBuilder.sortParam(pageable.getSort()));
        return LIST_VIEW;
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable String id, Model model) {
        model.addAttribute("item", itemService.findById(id));
        model.addAttribute("stockEntry", new StockEntryDTO());
        return DETAIL_VIEW;
    }

    @GetMapping("/new")
    @PreAuthorize(Permissions.ESTOQUE)
    public String newForm(Model model) {
        model.addAttribute("form", new ItemDTO());
        return FORM_VIEW;
    }

    @PostMapping
    @PreAuthorize(Permissions.ESTOQUE)
    public String create(@Valid @ModelAttribute("form") ItemDTO form,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) return FORM_VIEW;

        try {
            Item item = itemService.create(form);
            FlashMessage.success(redirectAttributes, "Item cadastrado com sucesso.");
            return REDIRECT_BASE + "/" + item.getId();
        } catch (BusinessException exception) {
            result.reject("business", exception.getMessage());
            return FORM_VIEW;
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize(Permissions.SUPERVISOR)
    public String editForm(@PathVariable String id, Model model) {
        model.addAttribute("form", itemMapper.toDTO(itemService.findById(id)));
        model.addAttribute("itemId", id);
        return FORM_VIEW;
    }

    @PostMapping("/{id}")
    @PreAuthorize(Permissions.SUPERVISOR)
    public String update(@PathVariable String id,
                         @Valid @ModelAttribute("form") ItemDTO form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        model.addAttribute("itemId", id);
        if (result.hasErrors()) return FORM_VIEW;

        try {
            itemService.update(id, form);
            FlashMessage.success(redirectAttributes, "Item atualizado com sucesso.");
            return REDIRECT_BASE + "/" + id;
        } catch (BusinessException exception) {
            result.reject("business", exception.getMessage());
            return FORM_VIEW;
        }
    }

    @PostMapping("/{id}/stock")
    @PreAuthorize(Permissions.ESTOQUE)
    public String addStock(@PathVariable String id,
                           @Valid @ModelAttribute("stockEntry") StockEntryDTO stockEntry,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {
        String redirectDetail = REDIRECT_BASE + "/" + id;

        if (result.hasErrors()) {
            FlashMessage.error(redirectAttributes, result.getAllErrors().get(0).getDefaultMessage());
            return redirectDetail;
        }

        try {
            itemService.addStock(id, stockEntry.getQuantity());
            FlashMessage.success(redirectAttributes,
                    "Entrada de %d unidade(s) registrada.".formatted(stockEntry.getQuantity()));
        } catch (BusinessException exception) {
            FlashMessage.error(redirectAttributes, exception.getMessage());
        }
        return redirectDetail;
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize(Permissions.GERENTE)
    public String delete(@PathVariable String id, RedirectAttributes redirectAttributes) {
        itemService.deleteById(id);
        FlashMessage.success(redirectAttributes, "Item excluído com sucesso.");
        return REDIRECT_BASE;
    }
}
