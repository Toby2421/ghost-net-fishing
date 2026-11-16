package de.ghostnet.web;
import de.ghostnet.service.*;
import jakarta.validation.Valid;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Controller
public class NetController {
    private final GhostNetService service;
    public NetController(GhostNetService service) { this.service=service; }
    @GetMapping("/")
    public String index(@RequestParam(defaultValue="false") boolean open, Model model) {
        model.addAttribute("nets", service.list(open)); model.addAttribute("openOnly", open); return "index";
    }
    @GetMapping("/nets/new")
    public String report(Model model) { model.addAttribute("reportForm", new ReportForm()); return "report"; }
    @PostMapping("/nets/new")
    public String report(@Valid @ModelAttribute ReportForm reportForm, BindingResult errors, RedirectAttributes flash) {
        if(errors.hasErrors()) return "report";
        service.report(reportForm); flash.addFlashAttribute("success", "Das Geisternetz wurde gemeldet."); return "redirect:/";
    }
    @GetMapping("/nets/{id}/{action:reserve|recover|missing}")
    public String action(@PathVariable Long id, @PathVariable String action, Model model) {
        model.addAttribute("contactForm", new ContactForm()); actionModel(id, action, model); return "action";
    }
    @PostMapping("/nets/{id}/{action:reserve|recover|missing}")
    public String action(@PathVariable Long id, @PathVariable String action,
        @Valid @ModelAttribute ContactForm contactForm, BindingResult errors, Model model, RedirectAttributes flash) {
        if(errors.hasErrors()) { actionModel(id,action,model); return "action"; }
        try {
            switch(action) {
                case "reserve" -> service.reserve(id, contactForm);
                case "recover" -> service.recover(id, contactForm);
                case "missing" -> service.missing(id, contactForm);
                default -> throw new BusinessException("Unbekannte Aktion.");
            }
        } catch(BusinessException ex) {
            errors.reject("business",ex.getMessage()); actionModel(id,action,model); return "action";
        }
        flash.addFlashAttribute("success", "Der Status wurde aktualisiert."); return "redirect:/";
    }
    private void actionModel(Long id, String action, Model model) {
        model.addAttribute("net",service.get(id)); model.addAttribute("action",action);
        model.addAttribute("title", switch(action) {
            case "reserve" -> "Bergung übernehmen";
            case "recover" -> "Geborgen melden";
            default -> "Verschollen melden";
        });
    }
    @ExceptionHandler(BusinessException.class)
    public String business(BusinessException ex, RedirectAttributes flash) {
        flash.addFlashAttribute("error",ex.getMessage()); return "redirect:/";
    }
    @ExceptionHandler(DataAccessException.class)
    public String conflict(DataAccessException ex, RedirectAttributes flash) {
        flash.addFlashAttribute("error","Speichern nicht möglich. Bitte Übersicht aktualisieren und erneut versuchen."); return "redirect:/";
    }
}
