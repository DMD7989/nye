import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import '../models/alert.dart';

class AlertCard extends StatelessWidget {
  final MissingAlert alert;
  final VoidCallback onTap;

  const AlertCard({super.key, required this.alert, required this.onTap});

  @override
  Widget build(BuildContext context) {
    final dateFormat = DateFormat('d MMM yyyy, HH:mm', 'fr_FR');

    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      clipBehavior: Clip.antiAlias,
      child: InkWell(
        onTap: onTap,
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            SizedBox(
              width: 96,
              height: 96,
              child: alert.photoPending || alert.photoUrl == null
                  ? Container(
                      color: Colors.grey.shade300,
                      child: const Icon(Icons.image_not_supported_outlined, color: Colors.grey),
                    )
                  : Image.network(
                      alert.photoUrl!,
                      fit: BoxFit.cover,
                      errorBuilder: (_, __, ___) => Container(
                        color: Colors.grey.shade300,
                        child: const Icon(Icons.broken_image_outlined, color: Colors.grey),
                      ),
                    ),
            ),
            Expanded(
              child: Padding(
                padding: const EdgeInsets.all(12),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Expanded(
                          child: Text(
                            alert.missingPersonName,
                            style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                            overflow: TextOverflow.ellipsis,
                          ),
                        ),
                        if (alert.restricted)
                          const Tooltip(
                            message: 'En attente de validation par un administrateur',
                            child: Icon(Icons.hourglass_top, size: 18, color: Colors.orange),
                          ),
                      ],
                    ),
                    const SizedBox(height: 4),
                    Text(
                      alert.description,
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(color: Colors.black54),
                    ),
                    const SizedBox(height: 6),
                    Row(
                      children: [
                        if (alert.distanceKm != null) ...[
                          const Icon(Icons.location_on, size: 14, color: Colors.grey),
                          Text(' ${alert.distanceKm!.toStringAsFixed(1)} km  ',
                              style: const TextStyle(fontSize: 12, color: Colors.grey)),
                        ],
                        Expanded(
                          child: Text(
                            dateFormat.format(alert.createdAt.toLocal()),
                            style: const TextStyle(fontSize: 12, color: Colors.grey),
                            overflow: TextOverflow.ellipsis,
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
