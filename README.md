# MailAndParcelCourier
MPC is a mail an parcel plugin with 3 different approches in the features.
The Features includes:
- E-Mail (Electronic) System.
- P-Mail (Physical) System
- Parcel System
- Ignore System for all 3 above.
- Mailbox System

# How to Install
To install InterfaceHub (IFH), proceed as follows:
- Download the Jar file.
- Copy the jar file into the plugins folder on all servers except the proxy(Bungeecord/Velocity etc).
- Restart all servers.
- Setup the Mysql Settings in the config.yml in the folder MailAndParcelCourier in all servers.
- Restart all servers.

# 3 Way System
The plugin enables a 3-way system.
- Electronic, in the sense of "only" commands in general. You only use emails. Parcels are also only sent with the command */parcel send ...*.
- Physical, in the sense that you are still dependent on commands, but individual elements are processed physically. This is how the P-mails come in. In addition to the shipping fee, paper is now also required as an item. Received P-mails are then opened in the air with a right click.
- Physical with mailboxes. Here, further elements are physicalized. For example, sending P-mails and parcels is now carried out via mailboxes. You now also pack your parcel with */parcel pack ...* and then have to go to a mailbox and interact with an empty hand plus a right click before you can send something away.

Of course you can use everything together or only allow certain areas for certain groups, etc., as everything is controlled by permissions that are in the commands.yml.

[See the wiki for an command overview which correlate to which.](https://github.com/Avankziar/MailAndParcelCourier/wiki/ENG-3-Way-System)