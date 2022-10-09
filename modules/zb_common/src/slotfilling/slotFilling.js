bind("preProcess", function (ctx) {
    // check is slotfilling required
    if (!ctx.intent || !ctx.intent.slots || !ctx.nluResults.selected.debugInfo) {
        return;
    }
    var knownSlots = ctx.nluResults.selected.debugInfo.slots;

    if (hasAllRequiredSlots(ctx.intent, knownSlots)) {
        return;
    }

    var properties = ctx.injector.slotfilling || createDefaultProperties();
    ctx.client["slotfilling-switch-context"] = ctx.parseTree;

    // switch context
    ctx.response.replies = [{
        type: "context-switch",
        targetBotId: "cailapub-slotfilling",
        targetState: "/SlotFilling/Start",
        parameters: {
            intent: ctx.intent,
            maxSlotRetries: properties.maxSlotRetries,
            stopOnAnyIntent: properties.stopOnAnyIntent,
            stopOnAnyIntentThreshold: properties.stopOnAnyIntentThreshold,
            slots: knownSlots,
            entities: ctx.entities,
            targetState: "/SlotFilling/return",
            cailaToken: $jsapi.cailaService.getCurrentClassifierToken()
        }
    }];

    ctx.session.slotFillingTargetState = ctx.nluResults.selected.clazz;

    // stop execution
    ctx.temp.targetState = "/SlotFilling/empty"
});

function createDefaultProperties() {
    return {
        maxSlotRetries: 2,
        stopOnAnyIntent: false,
        stopOnAnyIntentThreshold: 0.2
    }
}

function hasAllRequiredSlots(intent, foundSlots) {
    function hasSlot(found, slotName) {
        for (var i = 0; i < found.length; i++) {
            var fs = found[i]
            if (fs.name == slotName) {
                return true
            }
        }
        return false
    }
    for (var i = 0; i < intent.slots.length; i++) {
        var s = intent.slots[i]; // required slot
        if (hasSlot(foundSlots, s.name) === false) {
            return false
        }
    }
    return true
}

function extractValue(entity) {
    try {
        return JSON.parse(entity.value)
    } catch (e) {
        return entity.value;
    }
}

function returnFromSlotFilling(ctx) {
    var data = ctx.request.data;
    for (var i = 0; i < data.slots.length; i++) {
        var knownSlot = data.slots[i];
        var value = extractValue(knownSlot);

        if (!knownSlot.array) {
            ctx.parseTree["_" + knownSlot.name] = value;
            continue;
        }

        if (Array.isArray(ctx.parseTree["_" + knownSlot.name])) {
            ctx.parseTree["_" + knownSlot.name].push(value)
        } else {
            ctx.parseTree["_" + knownSlot.name] = [value]
        }
    }

    if (Array.isArray(data.entities)) {
        for (var i = 0; i < data.entities.length; i++) {
            var entity = data.entities[i];
            entity.value = extractValue(entity)
        }
    }

    // прокидываем данные интента от котлина в текущий контекст
    ctx.intent = data.intent;
    ctx.entities = data.entities;
    // переходим в стейт соответствующий интенту

    ctx.parseTree = _.extend(ctx.client["slotfilling-switch-context"], ctx.parseTree);

    $reactions.transition(ctx.session.slotFillingTargetState);
}


