package de.omegazirkel.risingworld.tools.bridge;

import net.risingworld.api.Plugin;

/** Optional reflection-only bridge to OZ - Factions. Absence is represented by null/false values. */
public final class FactionBridge {
    private final Plugin owner;
    public FactionBridge(Plugin owner) { this.owner=owner; }
    public boolean isAvailable() { return factions()!=null; }
    public Integer factionIdForPlayer(int playerDbId) { return integer("getPlayerFactionId",playerDbId); }
    public String factionRoleForPlayer(int playerDbId) { Object value=call("getPlayerFactionRole",playerDbId); return value instanceof String s?s:null; }
    public boolean isFactionLeader(int playerDbId) { return Boolean.TRUE.equals(call("isPlayerFactionLeader",playerDbId)); }
    public String factionAccountIdForPlayer(int playerDbId) { Object value=call("getPlayerFactionAccountId",playerDbId); return value instanceof String s?s:null; }
    public String factionAccountId(int factionId) { Object value=call("getFactionAccountId",factionId); return value instanceof String s?s:null; }
    public Long factionDefaultCurrencyBalance(int factionId) { Object value=call("getFactionAccountBalance",factionId); return value instanceof Number n?n.longValue():null; }
    public Integer memberLimit(int factionId) { return integer("getFactionMemberLimit",factionId); }
    public Integer claimLicenses(int factionId) { return integer("getFactionClaimLicenseCount",factionId); }
    public Integer traderLicenses(int factionId) { return integer("getFactionTraderLicenseCount",factionId); }
    public Integer crierLicenses(int factionId) { return integer("getFactionCrierLicenseCount",factionId); }
    public Integer serviceNpcLicenses(int factionId) { return integer("getFactionServiceLicenseCount",factionId); }
    public boolean canManageClaim(int playerDbId) { return Boolean.TRUE.equals(call("canPlayerFactionManageClaim",playerDbId)); }
    public boolean canManageTrader(int playerDbId) { return Boolean.TRUE.equals(call("canPlayerFactionManageTrader",playerDbId)); }
    public boolean canManageCrier(int playerDbId) { return Boolean.TRUE.equals(call("canPlayerFactionManageCrier",playerDbId)); }
    public boolean canManageServiceNpc(int playerDbId) { return Boolean.TRUE.equals(call("canPlayerFactionManageServiceNPC",playerDbId)); }
    private Integer integer(String method,int value) { Object result=call(method,value); return result instanceof Number n?n.intValue():null; }
    private Object call(String method,int value) { Plugin factions=factions(); if(factions==null)return null; try{return factions.getClass().getMethod(method,int.class).invoke(factions,value);}catch(ReflectiveOperationException ex){return null;} }
    private Plugin factions() { return owner==null?null:owner.getPluginByName("OZ - Factions"); }
}
