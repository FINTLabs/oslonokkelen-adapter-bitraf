package no.fint.provider.adapter.oslo.las.handler;

import no.fint.provider.adapter.AbstractSupportedActions;
import no.fint.model.ressurser.lasstyring.LasstyringActions;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class SupportedActions extends AbstractSupportedActions {

    /**
     * <p>
     * This is where you add the actions that are supported for your adapter.
     * Use the add() for single action and addAll() for all actions in the enum.
     * </p>
     * <pre>
     *  add(PwfaActions.GET_ALL_DOGS);
     *  add(PwfaActions.GET_DOG);
     * </pre>
     */
    @PostConstruct
    public void addSupportedActions() {
        add(LasstyringActions.GET_ALL_LAS);
        add(LasstyringActions.UPDATE_LAS);
    }

}
