export enum TimePlay {
  MINUTES0 = 0.0,
  MINUTES10 = 0.11,
  MINUTES20 = 0.22,
  MINUTES30 = 0.33,
  MINUTES40 = 0.44,
  MINUTES45 = 0.5,
  MINUTES50 = 0.56,
  MINUTES60 = 0.67,
  MINUTES70 = 0.78,
  MINUTES80 = 0.89,
  MINUTES90 = 1.0,
}

/**
 * 🔄 **Trouve la valeur `TimePlay` la plus proche d'un pourcentage donné**
 * @param percentage - Valeur entre `0.0` et `1.0`
 * @returns `TimePlay` correspondant
 */
export function fromPercentage(percentage: number): TimePlay {
  const values = Object.values(TimePlay).filter((v) => typeof v === 'number') as number[];

  return values.reduce((prev, curr) =>
    Math.abs(curr - percentage) < Math.abs(prev - percentage) ? curr : prev,
  ) as TimePlay;
}

/**
 * 🎨 **Convertit une valeur `TimePlay` en label lisible (ex: "45 min")**
 * @param value - Une valeur de `TimePlay`
 * @returns Label lisible (`30 min`, `45 min`, etc.)
 */
export function getTimePlayLabel(value: TimePlay): string {
  switch (value) {
    case TimePlay.MINUTES0:
      return '0 min';
    case TimePlay.MINUTES10:
      return '10 min';
    case TimePlay.MINUTES20:
      return '20 min';
    case TimePlay.MINUTES30:
      return '30 min';
    case TimePlay.MINUTES40:
      return '40 min';
    case TimePlay.MINUTES45:
      return '45 min (mi-temps)';
    case TimePlay.MINUTES50:
      return '50 min';
    case TimePlay.MINUTES60:
      return '60 min';
    case TimePlay.MINUTES70:
      return '70 min';
    case TimePlay.MINUTES80:
      return '80 min';
    case TimePlay.MINUTES90:
      return '90 min (fin du match)';
    default:
      return `${Math.round(value * 90)} min`;
  }
}
